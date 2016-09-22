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

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.MessageHandler;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
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
  final URI backend;

  /* Session between the frontend (browser) and Knox */
  Session frontendSession;

  /* Session between the backend (Zeppelin) and Knox */
  javax.websocket.Session backendSession;

  WebSocketContainer container;

  /**
   * Create an instance
   */
  public ProxySocket(URI backend) {
    super();
    this.backend = backend;
  }

  @Override
  public void onWebSocketConnect(Session frontEndSession) {
    super.onWebSocketConnect(frontEndSession);
    this.frontendSession = frontEndSession;

    System.out.println("Websocket connection established ..");

    /* Let's connect to the backend, this is where the Backend-to-frontend plumbing takes place */
    container = ContainerProvider.getWebSocketContainer();
    final ProxyClientSocket backendSocket = new ProxyClientSocket(getRemote());
    
    // Attempt Connect
    try {
      backendSession = container.connectToServer(backendSocket,
          backend);

      //backendSession.addMessageHandler(new WholeMessageCaptureHandler(getRemote()));
      //backendSession.addMessageHandler(new PartialMessageCaptureHandler(getRemote()));
      

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

    /* Proxy frontend message to backend */
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

  /**
   * Whole message handler for receiving and proxing messages received from
   * backend to Knox
   */
  public class WholeMessageCaptureHandler
      implements MessageHandler.Whole<String> {

    final RemoteEndpoint remote;

    public WholeMessageCaptureHandler(final RemoteEndpoint remote) {
      super();
      this.remote = remote;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.websocket.MessageHandler.Whole#onMessage(java.lang.Object)
     */
    @Override
    public void onMessage(String message) {

      System.out.println("Message Recieved from Backend " + message);
      /* Just Proxying stuff */
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

  }
  
  /**
   * Whole message handler for receiving and proxing messages received from
   * backend to Knox
   */
  public class PartialMessageCaptureHandler
      implements MessageHandler.Partial<String> {

    final RemoteEndpoint remote;

    public PartialMessageCaptureHandler(final RemoteEndpoint remote) {
      super();
      this.remote = remote;
    }

    /*
     * (non-Javadoc)
     * 
     * @see javax.websocket.MessageHandler.Whole#onMessage(java.lang.Object)
     */
    @Override
    public void onMessage(String message, boolean last) {

      System.out.println("Message Recieved from Backend " + message);
      /* Just Proxying stuff */
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


  }

}
