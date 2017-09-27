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
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotEmpty;

public abstract class AbstractServiceInstanceMutatingCommand extends AbstractServiceInstanceCommand {

  private Map<? extends String, ?> context;
  
  private Map<? extends String, ?> parameters;

  protected AbstractServiceInstanceMutatingCommand(final String instanceId,
                                                   @NotEmpty final String serviceId,
                                                   final String planId,
                                                   final Map<? extends String, ?> parameters,
                                                   final boolean acceptsIncomplete) {
    this(instanceId, serviceId, planId, null, parameters, acceptsIncomplete);
  }
  
  protected AbstractServiceInstanceMutatingCommand(final String instanceId,
                                                   @NotEmpty final String serviceId,
                                                   final String planId,
                                                   final Map<? extends String, ?> context,
                                                   final Map<? extends String, ?> parameters,
                                                   final boolean acceptsIncomplete) {
    super(instanceId, serviceId, planId, acceptsIncomplete);
    if (context == null || context.isEmpty()) {
      this.context = Collections.emptyMap();
    } else {
      this.context = Collections.unmodifiableMap(new HashMap<>(context));
    }
    if (parameters == null || parameters.isEmpty()) {
      this.parameters = Collections.emptyMap();
    } else {
      this.parameters = Collections.unmodifiableMap(new HashMap<>(parameters));
    }
  }

  public final Map<? extends String, ?> getContext() {
    return this.context;
  }
  
  public final Map<? extends String, ?> getParameters() {
    return this.parameters;
  }

}
