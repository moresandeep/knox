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

import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.services.GatewayServices;
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.server.WebSocketHandler;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * 
 *
 */
public class GatewayWebsocketHandler extends WebSocketHandler implements WebSocketCreator {

  /* FIXME naive assumption */
  private static String BACKEND = "ws://localhost:9995/ws";
  
  final GatewayConfig config;
  final GatewayServices services;
  
  // private ProxyGatewaySocket proxySocket = new ProxyGatewaySocket();
  private ProxySocket proxySocket = new ProxySocket(URI.create(BACKEND));
  

  
  
  /**
   * Create an instance
   * @param config
   * @param services
   */
  public GatewayWebsocketHandler(final GatewayConfig config, final GatewayServices services) {
    super();
    
    this.config = config;
    this.services = services;
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.eclipse.jetty.websocket.server.WebSocketHandler#configure(org.eclipse.
   * jetty.websocket.servlet.WebSocketServletFactory)
   */
  @Override
  public void configure(final WebSocketServletFactory factory) {
    factory.setCreator(this);

  }

  /* (non-Javadoc)
   * @see org.eclipse.jetty.websocket.servlet.WebSocketCreator#createWebSocket(org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest, org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse)
   */
  @Override
  public Object createWebSocket(ServletUpgradeRequest req,
      ServletUpgradeResponse resp) {
    /* Let's send a server upgrade request to the backend */
    /*ClientUpgradeRequest upgrade = new ClientUpgradeRequest();
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
      // TODO perhaps some logging here 
      throw new RuntimeException(e);
    } 
    
    // so far so good 
    proxySocket.setClientSocket(clientSocket);
    proxySocket.setClient(client);
    clientSocket.setProxySocket(proxySocket);
    */
    
    return proxySocket;
  }

}
