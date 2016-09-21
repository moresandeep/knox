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
package org.apache.hadoop.gateway.websockets.jsr356;

import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Socket that receives connection from the frontend.
 *
 */
@ServerEndpoint(value = "/")
public class ProxySocket {

  /**
   * Create an instance
   */
  public ProxySocket() {
    super();
  }

  @OnOpen
  public void onConnect(final Session sess) {

    System.out.println("Socket Connected: " + sess);
  }

  @OnMessage
  public void onWebSocketText(final String message) {
    System.out.println("Received TEXT message: " + message);
  }

  @OnClose
  public void onWebSocketClose(final CloseReason reason) {
    System.out.println("Socket Closed: " + reason);
  }

  @OnError
  public void onWebSocketError(final Throwable e) {
    e.printStackTrace(System.err);
  }

}
