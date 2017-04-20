package org.apache.hadoop.gateway.filter;

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

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.gateway.GatewayMessages;
import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.i18n.messages.MessagesFactory;
import org.apache.hadoop.gateway.services.GatewayServices;
import org.apache.hadoop.gateway.topology.Topology;
import org.eclipse.jetty.server.HttpConnection;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ScopedHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * This handler will be ONLY registered with a specific connector, listening on a port that
 * is configured through a property gateway.port.mapping.{topologyName} in gateway-site.xml
 * <p>
 * The function of this connector is to append the right context path (/({gateway}/{topology}) and forward the request
 * to the default port.
 * <p>
 * See KNOX-
 *
 * @since 0.13
 */

public class ForwardHandler extends ScopedHandler {

    private static final GatewayMessages LOG = MessagesFactory.get(GatewayMessages.class);

    private GatewayConfig config;
    private GatewayServices services;
    private Topology topology;

    private String redirectContext = null;
    private final int redirectPort;

    public ForwardHandler(final GatewayConfig config, final Topology topology, final GatewayServices services) {

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

        this.redirectPort = config.getGatewayPort();

        redirectContext = "/" + config.getGatewayPath() + "/" + topology.getName();

    }

    @Override
    public void doScope(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {

        final String newTarget = redirectContext + target;

        RedirectHandlerNew.ForwardedRequest newRequest = new RedirectHandlerNew.ForwardedRequest(request, redirectContext, newTarget);
        LOG.redirectHandlerForward(target, newTarget);

        /* if the request starts already has the /gateway/{topology} part then skip it */
        if(StringUtils.startsWithIgnoreCase(target, redirectContext)) {
            nextScope(target, baseRequest, newRequest, response);
        }
        else {
            nextScope(newTarget, baseRequest, newRequest, response);
        }

        //request.getRequestDispatcher(newTarget).forward(request, response);


    }

    @Override
    public void doHandle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response) throws IOException, ServletException {


        final String newTarget = redirectContext + target;

        final String uri = request.getScheme() + "://" + request.getServerName() + ":" + this.redirectPort + redirectContext +
                request.getRequestURI() +
                (request.getQueryString() != null ? "?" + request.getQueryString() : "");

        RedirectHandlerNew.ForwardedRequest newRequest = new RedirectHandlerNew.ForwardedRequest(request, redirectContext, uri);
        LOG.redirectHandlerForward(target, newTarget);

        /* if the request starts already has the /gateway/{topology} part then skip it */
        if(StringUtils.startsWithIgnoreCase(target, redirectContext)) {
            request.getRequestDispatcher(target).include(request, response);
        }
        else {
            request.getRequestDispatcher(newTarget).include(request, response);
        }

        //request.getRequestDispatcher(newTarget).forward(request, response);
        //request.getRequestDispatcher(newTarget).include(request, response);

        //request.getRequestDispatcher(target).include(request, response);

        Request base_request = (request instanceof Request) ? (Request)request: HttpConnection.getCurrentConnection().getHttpChannel().getRequest();
        base_request.setHandled(true);

        //nextHandle(newTarget, baseRequest, newRequest, response );


        //nextHandle(target, baseRequest, request, response);

        /*
        if (!baseRequest.isHandled()) {


            //final String uri = request.getScheme() + "://" + request.getServerName() + ":" + this.redirectPort + redirectContext +
                    request.getRequestURI() +
                    (request.getQueryString() != null ? "?" + request.getQueryString() : "");

            //LOG.redirectHandlerForward(target, uri);

            //response.sendRedirect(uri);




            final String newTarget = redirectContext + target;
            RedirectHandler.ForwardedRequest newRequest = new RedirectHandler.ForwardedRequest(request, newTarget);
            LOG.redirectHandlerForward(target, newTarget);

            //request.getRequestDispatcher(newTarget).forward(request, response);

            //super.handle(newTarget, baseRequest, newRequest, response);

            super.handle(target, baseRequest, request, response);
        }
        */
    }

    static class ForwardedRequest extends HttpServletRequestWrapper {

        private String newURL;
        private String contextpath;

        public ForwardedRequest( final HttpServletRequest request, final String contextpath, final String newURL ) {
            super( request );
            this.newURL = newURL;
            this.contextpath = contextpath;
        }

        @Override
        public StringBuffer getRequestURL() {
            final StringBuffer originalUrl = ((HttpServletRequest) getRequest()).getRequestURL();
            return new StringBuffer(newURL);
        }

        @Override
        public String getRequestURI() {
            return contextpath + super.getRequestURI();
        }

        @Override
        public String getContextPath() {
            return this.contextpath;
        }



    }

}
