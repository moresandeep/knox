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

import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.services.GatewayServices;
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
  
/* Websocket message configs */
  
  public final static int MAX_TEXT_MESSAGE_SIZE =  Integer.MAX_VALUE;
  
  public final static int MAX_BINARY_MESSAGE_SIZE =  Integer.MAX_VALUE;
  
  public final static int MAX_TEXT_MESSAGE_BUFFER_SIZE =  32768;
  
  public final static int MAX_BINARY_MESSAGE_BUFFER_SIZE =  32768;
  
  public final static int INPUT_BUFFER_SIZE = 4096;
  
  public final static int ASYNC_WRITE_TIMEOUT = 60000;
  
  public final static int IDLE_TIMEOUT = 300000;
  
  final GatewayConfig config;
  final GatewayServices services;
 
  
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
    factory.getPolicy().setMaxTextMessageSize(MAX_TEXT_MESSAGE_SIZE);
    factory.getPolicy().setMaxBinaryMessageSize(MAX_TEXT_MESSAGE_SIZE);
    
    factory.getPolicy().setMaxBinaryMessageBufferSize(MAX_BINARY_MESSAGE_BUFFER_SIZE);
    factory.getPolicy().setMaxTextMessageBufferSize(MAX_TEXT_MESSAGE_BUFFER_SIZE);
    
    factory.getPolicy().setInputBufferSize(INPUT_BUFFER_SIZE);
    
    factory.getPolicy().setAsyncWriteTimeout(ASYNC_WRITE_TIMEOUT);
    factory.getPolicy().setIdleTimeout(IDLE_TIMEOUT);

  }

  /* (non-Javadoc)
   * @see org.eclipse.jetty.websocket.servlet.WebSocketCreator#createWebSocket(org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest, org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse)
   */
  @Override
  public Object createWebSocket(ServletUpgradeRequest req,
      ServletUpgradeResponse resp) {
    
    /* Upgrade happens here */
    return new ProxySocket(URI.create(BACKEND));
  }

}
