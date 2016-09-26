/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.hadoop.gateway.websockets;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.MessageHandler;
import javax.websocket.WebSocketContainer;

import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

/**
 * This class acts as server
 *
 */
public class ProxySocket extends WebSocketAdapter {
  
  /* URI for the backend */
  private final URI backend;

  /* Session between the frontend (browser) and Knox */
  private Session frontendSession;

  /* Session between the backend (Zeppelin) and Knox */
  private javax.websocket.Session backendSession;

  private WebSocketContainer container;

  /**
   * Create an instance
   */
  public ProxySocket(URI backend) {
    super();
    this.backend = backend;
  }

  @Override
  public void onWebSocketConnect(final Session frontEndSession) {
    super.onWebSocketConnect(frontEndSession);
    this.frontendSession = frontEndSession;

    System.out.println("Websocket connection established ..");

    /*
     * Let's connect to the backend, this is where the Backend-to-frontend
     * plumbing takes place
     */
    container = ContainerProvider.getWebSocketContainer();
    final ProxyClientSocket backendSocket = new ProxyClientSocket(
        getMessageCallback());

    /* build the configuration */

    // Attempt Connect
    try {
      backendSession = container.connectToServer(backendSocket, backend);

      // backendSession.addMessageHandler(new
      // WholeMessageCaptureHandler(getRemote()));
      // backendSession.addMessageHandler(new
      // PartialMessageCaptureHandler(getRemote()));

    } catch (DeploymentException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Override
  public void onWebSocketBinary(final byte[] payload, final int offset,
      final int length) {

    if (isNotConnected()) {
      return;
    }

    // FIXME implement this
  }

  @Override
  public void onWebSocketText(final String message) {

    if (isNotConnected()) {
      return;
    }

    System.out.println("Incoming message : " + message);

    /* Proxy message to backend */
    try {
      backendSession.getBasicRemote().sendText(message);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    super.onWebSocketClose(statusCode, reason);

    System.out.println(String.format(
        "Closing websocket connection code %S, reason %s", statusCode, reason));
    closeQuietly();

  }

  @Override
  public void onWebSocketError(final Throwable t) {
    super.onWebSocketError(t);

    t.printStackTrace(System.err);
    closeQuietly();
  }

  private MessageEventCallback getMessageCallback() {

    return new MessageEventCallback() {

      @Override
      public void doCallback(String message) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onConnectionOpen(Object session) {
        // TODO Auto-generated method stub

      }

      @Override
      public void onConnectionClose(String reason) {
        // FIXME LOG
        closeQuietly();

      }

      @Override
      public void onError(Throwable cause) {
        // FIXME LOG
        closeQuietly();

      }

      @Override
      public void onMessageText(String message, Object session) {
        final RemoteEndpoint remote = getRemote();
        /* Proxy message to frontend */
        try {
          remote.sendString(message);
          if (remote.getBatchMode() == BatchMode.ON) {
            remote.flush();
          }
        } catch (IOException e) {
          // TODO Auto-generated catch block
          throw new RuntimeIOException(e);
        }

      }

      @Override
      public void onMessageBinary(byte[] message, boolean last,
          Object session) {
        // TODO Auto-generated method stub

      }

    };

  }

  private void closeQuietly() {

    try {
      backendSession.close();
    } catch (IOException e) {
      // TODO Need to log and check for null
    }

    if (container instanceof LifeCycle) {
      try {
        ((LifeCycle) container).stop();
      } catch (Exception e) {
        // TODO Need to log and check for null
      }
    }

    frontendSession.close();

  }

}
