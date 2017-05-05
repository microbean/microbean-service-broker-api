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

import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotEmpty;

import org.microbean.servicebroker.api.command.state.Operation;

public class ProvisionServiceInstanceCommand extends AbstractServiceInstanceMutatingCommand {

  private final String organizationGuid;

  private final String spaceGuid;
  
  public ProvisionServiceInstanceCommand(final String instanceId,
                                         @NotEmpty final String serviceId,
                                         @NotEmpty final String planId,
                                         final Map<? extends String, ?> parameters,
                                         final boolean acceptsIncomplete,
                                         @NotEmpty final String organizationGuid,
                                         @NotEmpty final String spaceGuid) {
    super(instanceId, serviceId, planId, parameters, acceptsIncomplete);
    Objects.requireNonNull(planId);
    Objects.requireNonNull(organizationGuid);
    Objects.requireNonNull(spaceGuid);
    this.organizationGuid = organizationGuid;
    this.spaceGuid = spaceGuid;
  }

  public final String getOrganizationGuid() {
    return this.organizationGuid;
  }

  public final String getSpaceGuid() {
    return this.spaceGuid;
  }

  public static class Response extends org.microbean.servicebroker.api.command.Response {

    private final URI dashboardUri;

    private final Operation operation;

    public Response() {
      this(null, null);
    }
    
    public Response(final URI dashboardUri) {
      this(dashboardUri, null);
    }

    public Response(final Operation operation) {
      this(null, operation);
    }
    
    public Response(final URI dashboardUri, final Operation operation) {
      super();
      this.dashboardUri = dashboardUri;
      this.operation = operation;
    }

    public final URI getDashboardUri() {
      return this.dashboardUri;
    }

    public final Operation getOperation() {
      return this.operation;
    }
    
  }
  
}
