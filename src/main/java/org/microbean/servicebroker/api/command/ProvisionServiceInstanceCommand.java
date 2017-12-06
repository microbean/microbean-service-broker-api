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

// import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class ProvisionServiceInstanceCommand extends AbstractServiceInstanceMutatingCommand {

  private final String organizationGuid;

  private final String spaceGuid;

  public ProvisionServiceInstanceCommand(final String instanceId,
                                         @NotNull /* @NotEmpty */ final String serviceId,
                                         @NotNull /* @NotEmpty */ final String planId,
                                         final Map<? extends String, ?> parameters,
                                         @NotNull /* @NotEmpty */ final String organizationGuid,
                                         @NotNull /* @NotEmpty */ final String spaceGuid) {
    this(instanceId, serviceId, planId, null, false, organizationGuid, spaceGuid, parameters);
  }
  
  public ProvisionServiceInstanceCommand(final String instanceId,
                                         @NotNull /* @NotEmpty */ final String serviceId,
                                         @NotNull /* @NotEmpty */ final String planId,
                                         final Map<? extends String, ?> parameters,
                                         final boolean acceptsIncomplete,
                                         @NotNull /* @NotEmpty */ final String organizationGuid,
                                         @NotNull /* @NotEmpty */ final String spaceGuid) {
    this(instanceId, serviceId, planId, null, acceptsIncomplete, organizationGuid, spaceGuid, parameters);
  }
  
  public ProvisionServiceInstanceCommand(final String instanceId,
                                         @NotNull /* @NotEmpty */ final String serviceId,
                                         @NotNull /* @NotEmpty */ final String planId,
                                         final Map<? extends String, ?> context,
                                         @NotNull /* @NotEmpty */ final String organizationGuid,
                                         @NotNull /* @NotEmpty */ final String spaceGuid,
                                         final Map<? extends String, ?> parameters) {
    this(instanceId, serviceId, planId, context, false, organizationGuid, spaceGuid, parameters);
  }
  
  public ProvisionServiceInstanceCommand(final String instanceId,
                                         @NotNull /* @NotEmpty */ final String serviceId,
                                         @NotNull /* @NotEmpty */ final String planId,
                                         final Map<? extends String, ?> context,
                                         final boolean acceptsIncomplete,
                                         @NotNull /* @NotEmpty */ final String organizationGuid,
                                         @NotNull /* @NotEmpty */ final String spaceGuid,
                                         final Map<? extends String, ?> parameters) {
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

  public static class Response extends org.microbean.servicebroker.api.command.AbstractProvisioningResponse {

    private final URI dashboardUri;

    public Response() {
      super(null);
      this.dashboardUri = null;
    }
    
    public Response(final URI dashboardUri) {
      super(null);
      this.dashboardUri = dashboardUri;
    }

    public Response(@NotNull /* @NotEmpty */ final String operation) {
      this(null, operation);
    }
    
    public Response(final URI dashboardUri, @NotNull /* @NotEmpty */ final String operation) {
      super(operation);
      this.dashboardUri = dashboardUri;
    }

    public final URI getDashboardUri() {
      return this.dashboardUri;
    }
    
  }
  
}
