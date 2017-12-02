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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotEmpty;

public class DeleteServiceInstanceCommand extends AbstractServiceInstanceCommand {

  public DeleteServiceInstanceCommand(final String instanceId,
                                      @NotEmpty final String serviceId,
                                      @NotEmpty final String planId) {
    this(instanceId, serviceId, planId, false);
  }
  
  public DeleteServiceInstanceCommand(final String instanceId,
                                      @NotEmpty final String serviceId,
                                      @NotEmpty final String planId,
                                      final boolean acceptsIncomplete) {
    super(instanceId, serviceId, planId, acceptsIncomplete);
  }

  public static class Response extends org.microbean.servicebroker.api.command.AbstractResponse {

    private final String operation;

    public Response() {
      super();
      this.operation = null;
    }
    
    public Response(@NotEmpty final String operation) {
      super();
      this.operation = operation;
    }

    public final String getOperation() {
      return this.operation;
    }
    
  }
  
}
