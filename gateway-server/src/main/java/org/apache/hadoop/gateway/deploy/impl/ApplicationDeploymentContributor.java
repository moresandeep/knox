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
package org.apache.hadoop.gateway.deploy.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.hadoop.gateway.config.GatewayConfig;
import org.apache.hadoop.gateway.config.impl.GatewayConfigImpl;
import org.apache.hadoop.gateway.deploy.DeploymentContext;
import org.apache.hadoop.gateway.deploy.DeploymentException;
import org.apache.hadoop.gateway.deploy.ServiceDeploymentContributorBase;
import org.apache.hadoop.gateway.descriptor.FilterDescriptor;
import org.apache.hadoop.gateway.descriptor.FilterParamDescriptor;
import org.apache.hadoop.gateway.descriptor.ResourceDescriptor;
import org.apache.hadoop.gateway.filter.XForwardedHeaderFilter;
import org.apache.hadoop.gateway.filter.rewrite.api.CookieScopeServletFilter;
import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriteRulesDescriptor;
import org.apache.hadoop.gateway.filter.rewrite.api.UrlRewriteRulesDescriptorFactory;
import org.apache.hadoop.gateway.service.definition.Policy;
import org.apache.hadoop.gateway.service.definition.Rewrite;
import org.apache.hadoop.gateway.service.definition.Route;
import org.apache.hadoop.gateway.service.definition.ServiceDefinition;
import org.apache.hadoop.gateway.topology.Application;
import org.apache.hadoop.gateway.topology.Service;
import org.apache.hadoop.gateway.topology.Version;

public class ApplicationDeploymentContributor extends ServiceDeploymentContributorBase {

  private static final String SERVICE_DEFINITION_FILE_NAME = "service.xml";
  private static final String REWRITE_RULES_FILE_NAME = "rewrite.xml";
  private static final String XFORWARDED_FILTER_NAME = "XForwardedHeaderFilter";
  private static final String XFORWARDED_FILTER_ROLE = "xforwardedheaders";
  private static final String COOKIE_SCOPING_FILTER_NAME = "CookieScopeServletFilter";
  private static final String COOKIE_SCOPING_FILTER_ROLE = "cookiescopef";

  private ServiceDefinition serviceDefinition;

  private UrlRewriteRulesDescriptor serviceRules;

  private static ServiceDefinition loadServiceDefinition( Application application, File file ) throws JAXBException, FileNotFoundException, IOException {
    ServiceDefinition definition;
    if( !file.exists() ) {
      definition = new ServiceDefinition();
      definition.setName( application.getName() );
      List<Route> routes = new ArrayList<Route>(1);
      Route route;
      route = new Route();
      route.setPath( "/?**" );
      routes.add( route );
      route = new Route();
      route.setPath( "/**?**" );
      routes.add( route );
      definition.setRoutes( routes );
    } else {
      JAXBContext context = JAXBContext.newInstance( ServiceDefinition.class );
      Unmarshaller unmarshaller = context.createUnmarshaller();
      try( FileInputStream inputStream = new FileInputStream( file ) ) {
          definition = (ServiceDefinition) unmarshaller.unmarshal( inputStream );
      }
    }
    return definition;
  }

  private static UrlRewriteRulesDescriptor loadRewriteRules( Application application, File file ) throws IOException {
    UrlRewriteRulesDescriptor rules;
    if( !file.exists() ) {
      rules = UrlRewriteRulesDescriptorFactory.load( "xml", new StringReader( "<rules/>" ) );
    } else {
      FileReader reader = new FileReader( file );
      rules = UrlRewriteRulesDescriptorFactory.load( "xml", reader );
      reader.close();
    }
    return rules;
  }

  public ApplicationDeploymentContributor( GatewayConfig config, Application application ) throws DeploymentException {
    try {
      File appsDir = new File( config.getGatewayApplicationsDir() );
      File appDir = new File( appsDir, application.getName() );
      File serviceFile = new File( appDir, SERVICE_DEFINITION_FILE_NAME );
      File rewriteFile = new File( appDir, REWRITE_RULES_FILE_NAME );
      serviceDefinition = loadServiceDefinition( application, serviceFile );
      serviceRules = loadRewriteRules( application, rewriteFile );
    } catch ( IOException e ) {
      throw new DeploymentException( "Failed to deploy application: " + application.getName(), e );
    } catch ( JAXBException e ){
      throw new DeploymentException( "Failed to deploy application: " + application.getName(), e );
    }
  }

  @Override
  public String getRole() {
    return serviceDefinition.getRole();
  }

  @Override
  public String getName() {
    return serviceDefinition.getName();
  }

  @Override
  public Version getVersion() {
    return new Version(serviceDefinition.getVersion());
  }

  @Override
  public void contributeService(DeploymentContext context, Service service) throws Exception {
    contributeRewriteRules(context, service);
    contributeResources(context, service);
  }

  private void contributeRewriteRules(DeploymentContext context, Service service) {
    if ( serviceRules != null ) {
      UrlRewriteRulesDescriptor clusterRules = context.getDescriptor("rewrite");
      // Coverity CID 1352312
      if( clusterRules != null ) {
        clusterRules.addRules( serviceRules );
      }
    }
  }

  private void contributeResources(DeploymentContext context, Service service) {
    Map<String, String> filterParams = new HashMap<String, String>();
    List<Route> bindings = serviceDefinition.getRoutes();
    for ( Route binding : bindings ) {
      List<Rewrite> filters = binding.getRewrites();
      if ( filters != null && !filters.isEmpty() ) {
        filterParams.clear();
        for ( Rewrite filter : filters ) {
          filterParams.put(filter.getTo(), filter.getApply());
        }
      }
      try {
        contributeResource(context, service, binding, filterParams);
      } catch ( URISyntaxException e ) {
        e.printStackTrace();
      }
    }

  }

  private void contributeResource( DeploymentContext context, Service service, Route binding, Map<String, String> filterParams) throws URISyntaxException {
    List<FilterParamDescriptor> params = new ArrayList<FilterParamDescriptor>();
    ResourceDescriptor resource = context.getGatewayDescriptor().addResource();
    resource.role(service.getRole());
    resource.pattern(binding.getPath());
    //add x-forwarded filter if enabled in config
    if (context.getGatewayConfig().isXForwardedEnabled()) {
      resource.addFilter().name(XFORWARDED_FILTER_NAME).role(XFORWARDED_FILTER_ROLE).impl(XForwardedHeaderFilter.class);
    }
    if (context.getGatewayConfig().isCookieScopingToPathEnabled()) {
      FilterDescriptor filter = resource.addFilter().name(COOKIE_SCOPING_FILTER_NAME).role(COOKIE_SCOPING_FILTER_ROLE).impl(CookieScopeServletFilter.class);
      filter.param().name(GatewayConfigImpl.HTTP_PATH).value(context.getGatewayConfig().getGatewayPath());
    }
    List<Policy> policyBindings = binding.getPolicies();
    if ( policyBindings == null ) {
      policyBindings = serviceDefinition.getPolicies();
    }
    if ( policyBindings == null ) {
      //add default set
      addDefaultPolicies(context, service, filterParams, params, resource);
    } else {
      addPolicies(context, service, filterParams, params, resource, policyBindings);
    }
  }

  private void addPolicies( DeploymentContext context, Service service, Map<String, String> filterParams, List<FilterParamDescriptor> params, ResourceDescriptor resource, List<Policy> policyBindings) throws URISyntaxException {
    for ( Policy policyBinding : policyBindings ) {
      String role = policyBinding.getRole();
      if ( role == null ) {
        throw new IllegalArgumentException("Policy defined has no role for service " + service.getName());
      }
      role = role.trim().toLowerCase();
      if ( role.equals("rewrite") ) {
        addRewriteFilter(context, service, filterParams, params, resource);
      } else if ( topologyContainsProviderType(context, role) ) {
        context.contributeFilter(service, resource, role, policyBinding.getName(), null);
      }
    }
  }

  private void addDefaultPolicies( DeploymentContext context, Service service, Map<String, String> filterParams, List<FilterParamDescriptor> params, ResourceDescriptor resource) throws URISyntaxException {
    addWebAppSecFilters(context, service, resource);
    addAuthenticationFilter(context, service, resource);
    addRewriteFilter(context, service, filterParams, params, resource);
    addIdentityAssertionFilter(context, service, resource);
    addAuthorizationFilter(context, service, resource);
  }

  private void addRewriteFilter( DeploymentContext context, Service service, Map<String, String> filterParams, List<FilterParamDescriptor> params, ResourceDescriptor resource) throws URISyntaxException {
    if ( !filterParams.isEmpty() ) {
      for ( Map.Entry<String, String> filterParam : filterParams.entrySet() ) {
        params.add(resource.createFilterParam().name(filterParam.getKey()).value(filterParam.getValue()));
      }
    }
    addRewriteFilter(context, service, resource, params);
  }

}
