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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

// import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class UpdateServiceInstanceCommand extends AbstractServiceInstanceMutatingCommand {

  private final PreviousValues previousValues;

  public UpdateServiceInstanceCommand(final String instanceId,                                      
                                      @NotNull /* @NotEmpty */ final String serviceId,
                                      final String planId,
                                      final Map</* @NotNull */ ? extends String, ?> parameters,
                                      final PreviousValues previousValues) {
    this(instanceId, null, serviceId, planId, parameters, false, previousValues);
  }
  
  public UpdateServiceInstanceCommand(final String instanceId,                                      
                                      @NotNull /* @NotEmpty */ final String serviceId,
                                      final String planId,
                                      final Map</* @NotNull */ ? extends String, ?> parameters,
                                      final boolean acceptsIncomplete,
                                      final PreviousValues previousValues) {
    this(instanceId, null, serviceId, planId, parameters, acceptsIncomplete, previousValues);
  }

  public UpdateServiceInstanceCommand(final String instanceId,
                                      final Map</* @NotNull */ ? extends String, ?> context,
                                      @NotNull /* @NotEmpty */ final String serviceId,
                                      final String planId,
                                      final Map</* @NotNull */ ? extends String, ?> parameters,
                                      final PreviousValues previousValues) {
    this(instanceId, context, serviceId, planId, parameters, false, previousValues);
  }
  
  public UpdateServiceInstanceCommand(final String instanceId,
                                      final Map</* @NotNull */ ? extends String, ?> context,
                                      @NotNull /* @NotEmpty */ final String serviceId,
                                      final String planId,
                                      final Map</* @NotNull */ ? extends String, ?> parameters,
                                      final boolean acceptsIncomplete,
                                      final PreviousValues previousValues) {
    super(instanceId, serviceId, planId, context, parameters, acceptsIncomplete);
    if (!Boolean.getBoolean("org.microbean.servicebroker.api.lenient")) {
      Objects.requireNonNull(serviceId, () -> "serviceId must not be null");
    }
    this.previousValues = previousValues;
  }

  public final PreviousValues getPreviousValues() {
    return this.previousValues;
  }
  
  public static class Response extends AbstractProvisioningResponse {

    public Response() {
      super(null);
    }
    
    public Response(/* @NotEmpty */ final String operation) {
      super(operation);
    }
    
  }

  public static class PreviousValues {

    @Deprecated
    private final String serviceId;

    private final String planId;

    private final String organizationId;

    private final String spaceId;

    public PreviousValues() {
      this(null, null, null, null);
    }
    
    public PreviousValues(@Deprecated final String serviceId, final String planId, final String organizationId, final String spaceId) {
      super();
      this.serviceId = serviceId;
      this.planId = planId;
      this.organizationId = organizationId;
      this.spaceId = spaceId;
    }

    public final String getServiceId() {
      return this.serviceId;
    }

    public final String getPlanId() {
      return this.planId;
    }

    public final String getOrganizationId() {
      return this.organizationId;
    }

    public final String getSpaceId() {
      return this.spaceId;
    }
    
  }
  
}
