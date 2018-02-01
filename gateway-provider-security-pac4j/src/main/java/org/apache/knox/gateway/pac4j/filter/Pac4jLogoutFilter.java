package org.apache.knox.gateway.pac4j.filter;

import org.apache.knox.gateway.i18n.messages.MessagesFactory;
import org.apache.knox.gateway.pac4j.Pac4jMessages;
import org.pac4j.core.context.J2EContext;
import org.pac4j.core.engine.DefaultLogoutLogic;
import org.pac4j.core.engine.LogoutLogic;
import org.pac4j.core.http.J2ENopHttpActionAdapter;
import org.pac4j.core.util.CommonHelper;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
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
 * Filter that takes care of logout functionality.
 *
 * @since 1.1.0
 */
public class Pac4jLogoutFilter extends Pac4jDispatcherFilter {

  private static Pac4jMessages log = MessagesFactory.get(Pac4jMessages.class);

  private String pac4jCallbackUrl;

  /* Knoxssout */
  private LogoutLogic logoutLogic = new DefaultLogoutLogic<>();

  /* Create an instance */
  public Pac4jLogoutFilter() {
    super();
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);
    pac4jCallbackUrl = filterConfig.getInitParameter(PAC4J_CALLBACK_URL);

    if (pac4jCallbackUrl == null) {
      log.ssoAuthenticationProviderUrlRequired();
      throw new ServletException("Required pac4j callback URL is missing.");
    }

    // add the callback parameter to know it's a callback
    pac4jCallbackUrl = CommonHelper.addParameter(pac4jCallbackUrl, PAC4J_CALLBACK_PARAMETER, "true");

  }

  @Override
  public void doFilter(final ServletRequest servletRequest,
      final ServletResponse servletResponse, final FilterChain filterChain)
      throws IOException, ServletException {

    final HttpServletRequest request = (HttpServletRequest) servletRequest;
    final HttpServletResponse response = (HttpServletResponse) servletResponse;

    /* Handle logout */
    if (request.getRequestURL().toString().contains("/api/v1/webssout")) {
      //FIXME
      final String defaultUrl = request.getRequestURL().toString();
      final String logoutpattern = ".*/api/v1/webssout.*";
      final boolean localLogout = true;
      final boolean destroySession = true;
      final boolean centralLogout = true;
      final J2EContext context = new J2EContext(request, response,
          securityFilter.getConfig().getSessionStore());

      logoutLogic.perform(context, securityFilter.getConfig(),
          J2ENopHttpActionAdapter.INSTANCE, defaultUrl, logoutpattern,
          localLogout, destroySession, centralLogout);
    }

  }

  @Override
  public void destroy() {
    super.destroy();
  }
}
