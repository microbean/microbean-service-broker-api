/* -*- mode: Java; c-basic-offset: 2; indent-tabs-mode: nil; coding: utf-8-unix -*-
 *
 * Copyright © 2017 MicroBean.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.microbean.servicebroker.api.command;

import java.net.URI;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

// import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.microbean.servicebroker.api.AbstractStatefulObject;

public class ProvisionBindingCommand extends AbstractBindingCommand {

  private final BindResource bindResource;

  private final Map<? extends String, ?> parameters;
  
  public ProvisionBindingCommand(@NotNull /* @NotEmpty */ final String serviceId,
                                 @NotNull /* @NotEmpty */ final String planId,
                                 final BindResource bindResource,
                                 final Map<? extends String, ?> parameters) {
    this(null, null, serviceId, planId, bindResource, parameters);
  }

  public ProvisionBindingCommand(final String bindingId,
                                 final String serviceInstanceId,
                                 @NotNull /* @NotEmpty */ final String serviceId,
                                 @NotNull /* @NotEmpty */ final String planId,
                                 final BindResource bindResource,
                                 final Map<? extends String, ?> parameters) {
    super(bindingId, serviceInstanceId, serviceId, planId);
    this.bindResource = bindResource;
    if (parameters == null || parameters.isEmpty()) {
      this.parameters = Collections.emptyMap();
    } else {
      this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }
  }

  public final Map<? extends String, ?> getParameters() {
    return this.parameters;
  }

  public final BindResource getBindResource() {
    return this.bindResource;
  }


  /*
   * Inner and nested classes.
   */

  
  public static class BindResource extends AbstractStatefulObject {

    private final String appGuid;

    private final URI route;

    public BindResource() {
      this(null, null);
    }
    
    public BindResource(final URI route) {
      this(null, route);
    }
    
    public BindResource(final String appGuid) {
      this(appGuid, null);
    }
    
    public BindResource(final String appGuid, final URI route) {
      super();
      this.appGuid = appGuid;
      this.route = route;
    }

    public final String getAppGuid() {
      return this.appGuid;
    }

    public final URI getRoute() {
      return this.route;
    }
    
  }
  
  public static class Response extends AbstractResponse {

    private final Map<? extends String, ?> credentials;

    private final URI syslogDrainUri;

    private final URI routeServiceUri;

    private final Set<? extends Map<? extends String, ?>> volumeMounts;

    public Response() {
      this(null, null, null, null);
    }

    public Response(final Map<? extends String, ?> credentials) {
      this(credentials, null, null, null);
    }
    
    public Response(final Map<? extends String, ?> credentials,
                    final URI syslogDrainUri,
                    final URI routeServiceUri,
                    final Set<? extends Map<? extends String, ?>> volumeMounts) {
      super();
      if (credentials == null || credentials.isEmpty()) {
        this.credentials = Collections.emptyMap();
      } else {
        this.credentials = Collections.unmodifiableMap(new HashMap<>(credentials));
      }
      this.syslogDrainUri = syslogDrainUri;
      this.routeServiceUri = routeServiceUri;
      if (volumeMounts == null || volumeMounts.isEmpty()) {
        this.volumeMounts = Collections.emptySet();
      } else {
        this.volumeMounts = Collections.unmodifiableSet(new HashSet<>(volumeMounts));
      }
    }

    public final Map<? extends String, ?> getCredentials() {
      return this.credentials;
    }

    public final URI getSyslogDrainUri() {
      return this.syslogDrainUri;
    }

    public final URI getRouteServiceUri() {
      return this.routeServiceUri;
    }

    public final Set<? extends Map<? extends String, ?>> getVolumeMounts() {
      return this.volumeMounts;
    }
    
  }
  
}
