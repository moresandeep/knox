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
package org.apache.hadoop.gateway.identityasserter.common.filter;

import org.apache.commons.io.IOUtils;
import org.apache.hadoop.gateway.SpiGatewayMessages;
import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.i18n.messages.MessagesFactory;
import org.apache.hadoop.gateway.security.PrimaryPrincipal;
import org.apache.hadoop.gateway.servlet.SynchronousServletInputStreamAdapter;
import org.apache.hadoop.gateway.util.HttpUtils;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class IdentityAsserterHttpServletRequestWrapper extends HttpServletRequestWrapper {

private static SpiGatewayMessages log = MessagesFactory.get( SpiGatewayMessages.class );

  private static final String PRINCIPAL_PARAM = "user.name";
  private static final String DOAS_PRINCIPAL_PARAM = "doAs";
  
  String username = null;

  public IdentityAsserterHttpServletRequestWrapper( HttpServletRequest request, String principal ) {
    super(request);
    username = principal;
  }

  @Override
  public Principal getUserPrincipal() {
    return new PrimaryPrincipal(username);
  }

  @Override
  public String getParameter(String name) {
    if (name.equals(PRINCIPAL_PARAM)) {
      return username;
    }
    return super.getParameter(name);
  }
  
  @SuppressWarnings("rawtypes")
  @Override
  public Map getParameterMap() {
    return getParams();
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public Enumeration getParameterNames() {
    Map<String, String[]> params = getParams();
    if (params == null) {
      params = new HashMap<String, String[]>();
    }
    Enumeration<String> e = Collections.enumeration((Collection<String>) params.keySet());

    return e;
  }

  @Override
  public String[] getParameterValues(String name) {
    Map<String, String[]> params = getParams();
    if (params == null) {
      params = new HashMap<String, String[]>();
    }

    return params.get(name);
  }

  private Map<String, String[]> getParams( String qString ) {
    Map<String, String[]> params = null;
    if (getMethod().equals("GET")) {
      if (qString != null && qString.length() > 0) {
        params = HttpUtils.parseQueryString( qString );
      }
      else {
        params = new HashMap<String, String[]>();
      }
    }
    else {
      if (qString == null || qString.length() == 0) {
        return null;
      }
      else {
        params = HttpUtils.parseQueryString( qString );
      }
    }  
    return params;
  }

  private Map<String, String[]> getParams() {
    return getParams( super.getQueryString() );
  }

  @Override
  public String getQueryString() {
    String q = null;
    Map<String, String[]> params = getParams();

    if (params == null) {
      params = new HashMap<String, String[]>();
    }
    
    ArrayList<String> al = new ArrayList<String>();
    al.add(username);
    String[] a = { "" };

    List<String> principalParamNames = getImpersonationParamNames();
    params = scrubOfExistingPrincipalParams(params, principalParamNames);

    if ("true".equals(System.getProperty(GatewayConfig.HADOOP_KERBEROS_SECURED))) {
      params.put(DOAS_PRINCIPAL_PARAM, al.toArray(a));
    } else {
      params.put(PRINCIPAL_PARAM, al.toArray(a));
    }

    String encoding = getCharacterEncoding();
    if (encoding == null) {
      encoding = Charset.defaultCharset().name();
    }
    q = urlEncode(params, encoding);
    return q;
  }

  private List<String> getImpersonationParamNames() {
    // TODO: let's have service definitions register their impersonation
    // params in a future release and get this list from a central registry.
    // This will provide better coverage of protection by removing any
    // prepopulated impersonation params.
    ArrayList<String> principalParamNames = new ArrayList<String>();
    principalParamNames.add(DOAS_PRINCIPAL_PARAM);
    principalParamNames.add(PRINCIPAL_PARAM);
    return principalParamNames;
  }

  private Map<String, String[]> scrubOfExistingPrincipalParams(
      Map<String, String[]> params, List<String> principalParamNames) {
    HashSet<String> remove = new HashSet<String>();
    for (String paramKey : params.keySet()) {
      for (String p : principalParamNames) {
        if (p.equalsIgnoreCase(paramKey)) {
          remove.add(paramKey);
          log.possibleIdentitySpoofingAttempt(paramKey);
        }
      }
    }
    params.keySet().removeAll(remove);
    return params;
  }

  @Override
  public int getContentLength() {
    int len;
    String contentType = getContentType();
    // If the content type is a form we might rewrite the body so default it to -1.
    if( contentType != null && contentType.startsWith( "application/x-www-form-urlencoded" ) ) {
      len = -1;
    } else {
      len = super.getContentLength();
    }
    return len;
  }

  @Override
  public ServletInputStream getInputStream() throws java.io.IOException {
    String contentType = getContentType();
    if( contentType != null && contentType.startsWith( "application/x-www-form-urlencoded" ) ) {
      String encoding = getCharacterEncoding();
      if( encoding == null ) {
        encoding = Charset.defaultCharset().name();
      }
      String body = IOUtils.toString( super.getInputStream(), encoding );
      Map<String, String[]> params = getParams( body );
      if (params == null) {
        params = new HashMap<String, String[]>();
      }
      body = urlEncode( params, encoding );
      // ASCII is OK here because the urlEncode about should have already escaped
      return new ServletInputStreamWrapper( new ByteArrayInputStream( body.getBytes( "US-ASCII" ) ) );
    } else {
      return super.getInputStream();
    }
  }

  static String urlEncode( String string, String encoding ) {
    try {
      return URLEncoder.encode( string, encoding );
    } catch (UnsupportedEncodingException e) {
      throw new UnsupportedOperationException(e);
    }
  }

  public static String urlEncode( Map<String, String[]> map, String encoding ) {
    StringBuilder sb = new StringBuilder();
    for( Map.Entry<String,String[]> entry : map.entrySet() ) {
      String name = entry.getKey();
      if( name != null && name.length() > 0 ) {
        String[] values = entry.getValue();
        if( values == null || values.length == 0 ) {
          sb.append( entry.getKey() );
        } else {
          for( int i = 0; i < values.length; i++ ) {
            String value = values[ i ];
              if( sb.length() > 0 ) {
                sb.append( "&" );
              }
              try {
                sb.append( urlEncode( name, encoding ) );
                if( value != null ) {
                  sb.append("=");
                  sb.append(urlEncode(value, encoding));
                }
              } catch( IllegalArgumentException e ) {
                log.skippingUnencodableParameter( name, value, encoding, e );
              }
          }
        }
      }
    }
    return sb.toString();
  }

  private static class ServletInputStreamWrapper extends SynchronousServletInputStreamAdapter {

    private InputStream stream;

    private ServletInputStreamWrapper( InputStream stream ) {
      this.stream = stream;
    }

    @Override
    public int read() throws IOException {
      return stream.read();
    }

  }

}
