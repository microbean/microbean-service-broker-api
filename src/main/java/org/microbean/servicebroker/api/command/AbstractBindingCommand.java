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

// import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public abstract class AbstractBindingCommand extends AbstractCommand {

  private String instanceId;

  private String bindingId;
  
  private final String serviceId;

  private final String planId;
  
  protected AbstractBindingCommand(final String bindingId,
                                   final String instanceId,
                                   @NotNull /* @NotEmpty */ final String serviceId,
                                   @NotNull /* @NotEmpty */ final String planId) {
    super();
    Objects.requireNonNull(serviceId);
    Objects.requireNonNull(planId);
    this.bindingId = bindingId;
    this.instanceId = instanceId;
    this.serviceId = serviceId;
    this.planId = planId;
  }

  public final String getBindingId() {
    return this.bindingId;
  }

  public final void setBindingId(@NotNull /* @NotEmpty */ final String bindingId) {
    this.bindingId = Objects.requireNonNull(bindingId);
  }

  public final String getInstanceId() {
    return this.instanceId;
  }

  public final void setInstanceId(@NotNull /* @NotEmpty */ final String instanceId) {
    this.instanceId = Objects.requireNonNull(instanceId);
  }
  
  public final String getServiceId() {
    return this.serviceId;
  }

  public final String getPlanId() {
    return this.planId;
  }
  
}
