package org.apache.hadoop.gateway.filter;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.HandlerWrapper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

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

/**
 * This is a helper handler that adjustss the "target" patch of the request.
 * Used when Topology Port Mapping feature is used.
 * See KNOX-928
 * @since 0.13
 */
public class PortMappingHelperHandler extends HandlerWrapper {

    public PortMappingHelperHandler() {

    }

    @Override
    public void handle(final String target, final Request baseRequest, final HttpServletRequest request, final HttpServletResponse response ) throws IOException, ServletException {

        /*
        String context = "";
        String baseURI = baseRequest.getUri().toString();

        if(!baseURI.startsWith(target) ) {

            final int index = StringUtils.ordinalIndexOf(baseURI, "/", 3);
            if(index > 0) {
                context = baseURI.substring(0, index);
            }
        }

        super.handle(context+target, baseRequest, request, response);
        */

        String context = "";
        String baseURI = baseRequest.getUri().toString();

        /* extract the gateway specific part */
        String gatewayPath = "";
        final int targetIndex = StringUtils.ordinalIndexOf(target, "/", 3);
        if(targetIndex > 0) {
            gatewayPath = target.substring(0, targetIndex + 1);
        }

        if(!baseURI.startsWith(gatewayPath) ) {

            final int index = StringUtils.ordinalIndexOf(baseURI, "/", 3);
            if(index > 0) {
                context = baseURI.substring(0, index);
            }
        }

        super.handle(context+target, baseRequest, request, response);


    }
}
