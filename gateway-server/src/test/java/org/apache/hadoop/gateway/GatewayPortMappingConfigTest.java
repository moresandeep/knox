package org.apache.hadoop.gateway;

/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.services.DefaultGatewayServices;
import org.apache.hadoop.gateway.services.topology.TopologyService;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.easymock.EasyMock;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerCollection;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Test the Gateway Topology Port Mapping config
 *
 * @since 0.13
 */
public class GatewayPortMappingConfigTest {

  /**
   * Mock gateway config
   */
  private static GatewayConfig gatewayConfig;

  private static int eeriePort;
  private static int ontarioPort;
  private static int huronPort;

  private static int defaultPort;

  private static DefaultGatewayServices services;
  private static TopologyService topos;

  private static VelocityEngine velocity;
  private static VelocityContext context;

  private static Server gatewayServer;

  private static Properties params;

  @Rule
  public ExpectedException exception = ExpectedException.none();

  public GatewayPortMappingConfigTest() {
    super();
  }

  @BeforeClass
  public static void init() throws Exception {

    Map<String, Integer> topologyPortMapping = new ConcurrentHashMap<String, Integer>();

        /* get unique ports */
    eeriePort = getAvailablePort(1240, 49151);
    ontarioPort = getAvailablePort(eeriePort + 1, 49151);
    huronPort = getAvailablePort(ontarioPort + 1, 49151);

    defaultPort = getAvailablePort(huronPort + 1, 49151);

    topologyPortMapping.put("eerie", eeriePort);
    topologyPortMapping.put("ontario", ontarioPort);
    topologyPortMapping.put("huron", huronPort);

    gatewayConfig = EasyMock.createNiceMock(GatewayConfig.class);
    EasyMock.expect(gatewayConfig.getGatewayPortMappings())
        .andReturn(topologyPortMapping).anyTimes();

    EasyMock.expect(gatewayConfig.getGatewayPort()).andReturn(defaultPort)
        .anyTimes();

    EasyMock.replay(gatewayConfig);

    /* Start gateway to check port conflicts */
    startGatewayServer();

  }

  @AfterClass
  public static void stopServers() {
    try {
      gatewayServer.stop();
    } catch (final Exception e) {
      e.printStackTrace(System.err);
    }
  }

  /**
   * This utility method will return the next available port
   * that can be used.
   *
   * @return Port that is available.
   */
  public static int getAvailablePort(final int min, final int max) {

    for (int i = min; i <= max; i++) {

      if (!GatewayServer.isPortInUse(i)) {
        return i;
      }
    }
        /* too bad */
    return -1;
  }



  /**
   * This method simply tests the configs
   */
  @Test
  public void testGatewayConfig() {
    assertThat(gatewayConfig.getGatewayPortMappings().get("eerie"),
        greaterThan(-1));
    assertThat(gatewayConfig.getGatewayPortMappings().get("ontario"),
        greaterThan(-1));
    assertThat(gatewayConfig.getGatewayPortMappings().get("huron"),
        greaterThan(-1));
  }

  /**
   * Test case where topologies "eerie" and "huron" use same ports.
   */
  @Test
  public void testCheckPortConflict() throws IOException {
    /* Check port conflict with default port */
    exception.expect(IOException.class);
    exception.expectMessage(String.format(
        " Topologies %s and %s use the same port %d, ports for topologies (if defined) have to be unique. ",
        "huron", "eerie", huronPort));

    GatewayServer.checkPortConflict(huronPort, "eerie", gatewayConfig);

  }

  /**
   * Test a case where gateway is already running and same port is used to start
   * another gateway.
   *
   * @throws IOException
   */
  @Test
  public void testDefaultPortInUse() throws IOException {

    exception.expect(IOException.class);
    exception
        .expectMessage(String.format("Port %d already in use.", defaultPort));

    GatewayServer.checkPortConflict(defaultPort, null, gatewayConfig);

  }

  private static void startGatewayServer() throws Exception {
    /* use default Max threads */
    gatewayServer = new Server(defaultPort);
    final ServerConnector connector = new ServerConnector(gatewayServer);
    gatewayServer.addConnector(connector);

    /* workaround so we can add our handler later at runtime */
    HandlerCollection handlers = new HandlerCollection(true);

    /* add some initial handlers */
    ContextHandler context = new ContextHandler();
    context.setContextPath("/");
    handlers.addHandler(context);

    gatewayServer.setHandler(handlers);

    // Start Server
    gatewayServer.start();

  }

}
