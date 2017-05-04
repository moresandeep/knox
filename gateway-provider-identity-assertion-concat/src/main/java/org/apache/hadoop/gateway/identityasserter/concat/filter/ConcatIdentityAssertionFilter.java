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
package org.apache.hadoop.gateway.identityasserter.concat.filter;

import javax.security.auth.Subject;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import org.apache.hadoop.gateway.identityasserter.common.filter.CommonIdentityAssertionFilter;

public class ConcatIdentityAssertionFilter extends CommonIdentityAssertionFilter {
  private String prefix = null;
  private String suffix = null;
  
  /* (non-Javadoc)
   * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
   */
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    super.init(filterConfig);

    prefix = filterConfig.getInitParameter("concat.prefix");
    suffix = filterConfig.getInitParameter("concat.suffix");
    if (prefix == null) {
      prefix = "";
    }
    if (suffix == null) {
      suffix = "";
    }
  }

  /* (non-Javadoc)
   * @see org.apache.hadoop.gateway.identityasserter.common.filter.AbstractIdentityAssertionFilter#mapGroupPrincipals(java.lang.String, javax.security.auth.Subject)
   */
  @Override
  public String[] mapGroupPrincipals(String mappedPrincipalName, Subject subject) {
    return null;
  }

  /* (non-Javadoc)
   * @see org.apache.hadoop.gateway.identityasserter.common.filter.AbstractIdentityAssertionFilter#mapUserPrincipal(java.lang.String)
   */
  @Override
  public String mapUserPrincipal(String principalName) {
    return prefix + principalName + suffix;
  }
}
