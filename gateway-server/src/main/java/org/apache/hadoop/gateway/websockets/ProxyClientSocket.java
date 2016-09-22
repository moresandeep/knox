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

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;

import org.eclipse.jetty.io.RuntimeIOException;
import org.eclipse.jetty.websocket.api.BatchMode;
import org.eclipse.jetty.websocket.api.RemoteEndpoint;

/**
 * A websocket client
 *
 */
@ClientEndpoint
public class ProxyClientSocket {

  /**
   * Frontend endpoint to stream the data back.
   */
  final RemoteEndpoint remote;
  
  /**
    * Create an instance
    */
    public ProxyClientSocket(final RemoteEndpoint remote) {
      super();
      this.remote = remote;
    }
    
    /* Client methods */
    @OnOpen
    public void onClientOpen(final javax.websocket.Session backendSession) {
      // this.backendSession = backendSession;

      System.out.println("Opened a client connection with Zeppelin backend");

    }

    @OnClose
    public void onClientClose(final CloseReason reason) {
      System.out.println(
          "Closed client connection with Zeppelin backend reason: " + reason);
    }

    @OnError
    public void onClientError(Throwable cause) {
      cause.printStackTrace(System.err);
    }

    @OnMessage
    public void onBackendMessage(final String message, final javax.websocket.Session session) {
      System.out.println("$$$ Message came from the backend server ! " + message);
      
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

}
