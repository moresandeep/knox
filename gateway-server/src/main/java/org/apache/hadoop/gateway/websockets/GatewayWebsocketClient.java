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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.StatusCode;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

/**
 * 
 *
 */
@WebSocket(maxTextMessageSize = 64 * 1024)
public class GatewayWebsocketClient {

  private Session session;
  private ProxyGatewaySocket proxySocket;

  /**
   * Create an instance
   */
  public GatewayWebsocketClient() {
    super();
  }

  @OnWebSocketClose
  public void onClose(final int statusCode, final String reason) {
    // System.out.printf("Connection closed: %d - %s%n",statusCode,reason);
    this.session = null;
  }

  @OnWebSocketConnect
  public void onConnect(final Session session) {
    // System.out.printf("Got connect: %s%n",session);
    this.session = session;
    /*
     * try { Future<Void> fut; fut =
     * session.getRemote().sendStringByFuture("Hello");
     * fut.get(2,TimeUnit.SECONDS); // wait for send to complete.
     * 
     * fut =
     * session.getRemote().sendStringByFuture("Thanks for the conversation.");
     * fut.get(2,TimeUnit.SECONDS); // wait for send to complete.
     * 
     * session.close(StatusCode.NORMAL,"I'm done"); } catch (Throwable t) {
     * t.printStackTrace(); }
     */
  }

  @OnWebSocketMessage
  public void onMessage(final String msg) {
    //System.out.printf("Got msg: %s%n",msg);
    proxySocket.sendReply(msg);
  }

  public void sendMessage(final String message) {

    try {
      session.getRemote().sendString(message);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  /**
   * @return the proxySocket
   */
  public ProxyGatewaySocket getProxySocket() {
    return proxySocket;
  }

  /**
   * @param proxySocket the proxySocket to set
   */
  public void setProxySocket(ProxyGatewaySocket proxySocket) {
    this.proxySocket = proxySocket;
  }

}
