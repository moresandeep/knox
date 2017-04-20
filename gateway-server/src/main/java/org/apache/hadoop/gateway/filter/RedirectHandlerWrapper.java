package org.apache.hadoop.gateway.filter;

import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.services.GatewayServices;
import org.apache.hadoop.gateway.topology.Topology;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import java.util.Collection;
import java.util.List;

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
public class RedirectHandlerWrapper extends HandlerWrapper {


    private GatewayConfig config;
    private GatewayServices services;
    private Topology topology;
    private Handler deligate;
    private List<ContextHandler> topologyHandlers;

    public RedirectHandlerWrapper(final GatewayConfig config, final Collection<Topology> topologies, final GatewayServices services, final Handler deligate) {

        super();

        if (config == null) {
            throw new IllegalArgumentException("config==null");
        }
        if (services == null) {
            throw new IllegalArgumentException("services==null");
        }
        if (topology == null) {
            throw new IllegalArgumentException("topology==null");
        }
        this.config = config;
        this.services = services;
        this.topology = topology;

        for (final Topology t : topologies) {
            //FIXME: Check for port name and flag here, for now we'll do this for sandbox
            if (t.getName().equalsIgnoreCase("sandbox")) {
                final ContextHandler topologyHandler = new ContextHandler();

                final RedirectHandler redirectHandler = new RedirectHandler(config, t, services);
                redirectHandler.setHandler(deligate);

                topologyHandler.setHandler(redirectHandler);
                topologyHandler.setVirtualHosts(new String[] {"@"+t.getName().toLowerCase()});
                topologyHandler.setContextPath("/");

                topologyHandlers.add(topologyHandler);

                //contexts.addHandler(topologyHandler);

                //handlers.addHandler(topologyHandler);
            }
        }
    }



}
