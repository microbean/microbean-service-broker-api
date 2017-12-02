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

/**
 * @see <a
 * href="https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#response-4">The
 * Open Service Broker Specification, version 2.13</a>
 */
public class IdenticalBindingAlreadyExistsException extends BindingAlreadyExistsException {

  private static final long serialVersionUID = 1L;

  public IdenticalBindingAlreadyExistsException(final String message, final Throwable cause, final AbstractResponse response) {
    super(message, cause, response);
  }
  
}
