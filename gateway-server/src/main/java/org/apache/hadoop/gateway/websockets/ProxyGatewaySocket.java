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
import java.nio.ByteBuffer;

import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.client.WebSocketClient;

/**
 * 
 *
 */
public class ProxyGatewaySocket extends WebSocketAdapter {

  private GatewayWebsocketClient clientSocket;
  private WebSocketClient client; 

  /**
   * Create an instance
   */
  public ProxyGatewaySocket() {
    super();
  }

  @Override
  public void onWebSocketBinary(final byte[] payload, final int offset,
      final int length) {

    if (isNotConnected()) {
      return;
    }

    try {
      final ByteBuffer buffer = ByteBuffer.wrap(payload, offset, length);
      final RemoteEndpoint remote = getRemote();
      remote.sendBytes(buffer);

      if (remote.getBatchMode() == BatchMode.ON) {
        remote.flush();
      }

    } catch (final IOException e) {
      // TODO log this
      throw new RuntimeIOException(e);
    }

  }

  @Override
  public void onWebSocketText(final String message) {

    if (isNotConnected()) {
      return;
    }

      
      clientSocket.sendMessage(message);
      
      /*
      final RemoteEndpoint remote = getRemote();
      remote.sendString(message);
      if (remote.getBatchMode() == BatchMode.ON) {
        remote.flush();
      }
      */


  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    super.onWebSocketClose(statusCode, reason);
    
    try {
    if(client.isStopped())
    {
      client.stop();
    }    
    } catch (final Exception e) {
      //FIXME 
      e.printStackTrace();
    }

  }
  
  /**
   * Send reply back from backend 
   * @param reply
   */
  public void sendReply(final String reply) {
    
    final RemoteEndpoint remote = getRemote();
    try {
      remote.sendString(reply);
      if (remote.getBatchMode() == BatchMode.ON) {
        remote.flush();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      throw new RuntimeIOException(e);
    }
    
  }
  
  /**
   * @return the clientSocket
   */
  public GatewayWebsocketClient getClientSocket() {
    return clientSocket;
  }

  /**
   * @param clientSocket the clientSocket to set
   */
  public void setClientSocket(GatewayWebsocketClient clientSocket) {
    this.clientSocket = clientSocket;
  }

  /**
   * @return the client
   */
  public WebSocketClient getClient() {
    return client;
  }

  /**
   * @param client the client to set
   */
  public void setClient(WebSocketClient client) {
    this.client = client;
  }


}
