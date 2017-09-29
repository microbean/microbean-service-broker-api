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

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.microbean.servicebroker.api.command.state.Operation;

public class UpdateServiceInstanceCommand extends AbstractServiceInstanceMutatingCommand {

  private final PreviousValues previousValues;
  
  public UpdateServiceInstanceCommand(final String instanceId,                                      
                                      @NotEmpty final String serviceId,
                                      @NotEmpty final String planId,
                                      final Map<@NotNull ? extends String, ?> parameters,
                                      final boolean acceptsIncomplete,
                                      final PreviousValues previousValues) {
    this(instanceId, null, serviceId, planId, parameters, acceptsIncomplete, previousValues);
  }

  public UpdateServiceInstanceCommand(final String instanceId,
                                      final Map<@NotNull ? extends String, ?> context,
                                      @NotEmpty final String serviceId,
                                      @NotEmpty final String planId,
                                      final Map<@NotNull ? extends String, ?> parameters,
                                      final boolean acceptsIncomplete,
                                      final PreviousValues previousValues) {
    super(instanceId, serviceId, planId, context, parameters, acceptsIncomplete);
    this.previousValues = previousValues;
  }

  public final PreviousValues getPreviousValues() {
    return this.previousValues;
  }
  
  public static class Response extends org.microbean.servicebroker.api.command.Response {

    private final Operation operation;

    public Response(final Operation operation) {
      super();
      this.operation = operation;
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
    
  }
  
}
