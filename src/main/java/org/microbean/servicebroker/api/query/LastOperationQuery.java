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
package org.microbean.servicebroker.api.query;

import java.util.Objects;

import javax.validation.constraints.NotEmpty;

public class LastOperationQuery extends AbstractQuery {

  private final String serviceId;

  private String instanceId;
  
  private final String planId;
  
  private final String operationId;

  public LastOperationQuery(final String serviceId,
                            final String serviceInstanceId,
                            final String planId,
                            final String operationId) {
    super();
    this.serviceId = serviceId;
    this.instanceId = instanceId;
    this.planId = planId;
    this.operationId = operationId;
  }

  public final String getServiceId() {
    return this.serviceId;
  }

  @NotEmpty
  public final String getInstanceId() {
    return this.instanceId;
  }

  public final void setInstanceId(@NotEmpty final String instanceId) {
    this.instanceId = Objects.requireNonNull(instanceId);
  }

  public final String getPlanId() {
    return this.planId;
  }

  public final String getOperationId() {
    return this.operationId;
  }
  
}
