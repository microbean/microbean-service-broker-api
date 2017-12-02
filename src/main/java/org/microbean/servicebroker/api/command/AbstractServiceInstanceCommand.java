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

import java.util.Objects;

import javax.validation.constraints.NotEmpty;

public abstract class AbstractServiceInstanceCommand extends AbstractCommand {

  private String instanceId;

  @NotEmpty
  private final String serviceId;

  @NotEmpty
  private final String planId;

  private boolean acceptsIncomplete;

  protected AbstractServiceInstanceCommand(final String instanceId,
                                           @NotEmpty final String serviceId,
                                           @NotEmpty final String planId) {
    this(instanceId, serviceId, planId, false);
  }
  
  protected AbstractServiceInstanceCommand(final String instanceId,
                                           @NotEmpty final String serviceId,
                                           @NotEmpty final String planId,
                                           final boolean acceptsIncomplete) {
    super();
    Objects.requireNonNull(serviceId);
    this.instanceId = instanceId;
    this.serviceId = serviceId;
    this.planId = planId;
    this.acceptsIncomplete = acceptsIncomplete;
  }

  public final String getInstanceId() {
    return this.instanceId;
  }

  public final void setInstanceId(@NotEmpty final String instanceId) {
    Objects.requireNonNull(instanceId);
    this.instanceId = instanceId;
  }

  @NotEmpty
  public final String getServiceId() {
    return this.serviceId;
  }

  @NotEmpty
  public final String getPlanId() {
    return this.planId;
  }

  public boolean getAcceptsIncomplete() {
    return this.acceptsIncomplete;
  }

  public void setAcceptsIncomplete(final boolean acceptsIncomplete) {
    this.acceptsIncomplete = acceptsIncomplete;
  }

}
