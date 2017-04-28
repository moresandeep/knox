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
import org.easymock.EasyMock;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

/**
 * Test the Gateway Topology Port Mapping functionality
 * @since 0.13
 */
public class GatewayPortMappingTest {

    /**
     * Mock gateway config
     */
    private static GatewayConfig gatewayConfig;

    public GatewayPortMappingTest() {
        super();
    }

    @BeforeClass
    public static void init() {

        Map<String, Integer> topologyPortMapping = new ConcurrentHashMap<String, Integer>();

        /* get unique ports */
        int port1 = getAvailablePort(1240, 49151);
        int port2 = getAvailablePort(port1+1, 49151);
        int port3 = getAvailablePort(port2+1, 49151);

        topologyPortMapping.put("eerie", port1);
        topologyPortMapping.put("ontario", port2);
        topologyPortMapping.put("huron", port3);

        gatewayConfig = EasyMock.createNiceMock(GatewayConfig.class);
        EasyMock.expect(gatewayConfig.getGatewayPortMappings())
                .andReturn(topologyPortMapping).anyTimes();
        EasyMock.replay( gatewayConfig );

    }

    /**
     * This method simply tests the configs
     */
    @Test
    public void testGatewayConfig() {
        assertThat(gatewayConfig.getGatewayPortMappings().get("eerie"), greaterThan(-1) );
        assertThat(gatewayConfig.getGatewayPortMappings().get("ontario"), greaterThan(-1) );
        assertThat(gatewayConfig.getGatewayPortMappings().get("huron"), greaterThan(-1) );
    }


    /**
     * This utility method will return the next available port
     * that can be used.
     * @return Port that is available.
     */
    public static int getAvailablePort(final int min, final int max) {

        for(int i=min; i<=max; i++) {

            try (final ServerSocket ss = new ServerSocket(i)) {
                ss.setReuseAddress(true);
            }catch (IOException ignored) {
                /* can't connect must be free ! */
                return i;
            }
        }
        /* too bad */
        return -1;
    }
}
