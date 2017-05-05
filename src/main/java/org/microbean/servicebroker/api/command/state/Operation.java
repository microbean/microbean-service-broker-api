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
package org.microbean.servicebroker.api.command.state;

/**
 * Represents the {@code operation} field of a valid <a
 * href="https://github.com/openservicebrokerapi/servicebroker/blob/master/spec.md#provisioning"
 * target="_parent">Open Service Broker API provision service instance
 * response</a>.
 */
public class Operation {

  private final String id;

  public Operation(final String id) {
    super();
    this.id = id;
  }

  public String getId() {
    return this.id;
  }
  
}
