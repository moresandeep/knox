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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.eclipse.jetty.websocket.api.extensions.ExtensionConfig;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;

/**
 * 
 *
 */
public class GatewayWebsocketCreator implements WebSocketCreator {

  private ProxyGatewaySocket proxySocket = new ProxyGatewaySocket();
  

  /* FIXME naive assumption */
  private static String backend = "ws://localhost:9995/ws";

  /**
   * Create an instance
   */
  public GatewayWebsocketCreator() {
    super();
  }

  @Override
  public Object createWebSocket(final ServletUpgradeRequest req,
      final ServletUpgradeResponse resp) {

    /* Let's send a server upgrade request to the backend */
    ClientUpgradeRequest upgrade = new ClientUpgradeRequest();
    GatewayWebsocketClient clientSocket = new GatewayWebsocketClient();
    final WebSocketClient client = new WebSocketClient();
    
    try {
      
      client.start();
      

      client.getPolicy().setIdleTimeout(10000);

      URI backendURI = new URI(backend);
      
      upgrade.setSubProtocols(req.getSubProtocols());
      
      //ExtensionConfig[] extConfigs = req.getExtensions().toArray(new ExtensionConfig[0]);      
      //upgrade.addExtensions(extConfigs);
      
      // FIXME set other props too
      client.connect(clientSocket, backendURI, upgrade);

    } catch (URISyntaxException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (Exception e) {      
      /* TODO perhaps some logging here */
      throw new RuntimeException(e);
    } 
    
    /* so far so good */
    proxySocket.setClientSocket(clientSocket);
    proxySocket.setClient(client);
    clientSocket.setProxySocket(proxySocket);
    
    return proxySocket;
  }
}
