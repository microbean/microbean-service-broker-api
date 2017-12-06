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

import org.microbean.servicebroker.api.ServiceBrokerException;

public abstract class AbstractCommandException extends ServiceBrokerException {

  private static final long serialVersionUID = 1L;

  private final AbstractCommand command;

  protected AbstractCommandException(final AbstractCommand command) {
    super();
    this.command = command;
  }
  
  protected AbstractCommandException(final Throwable cause) {
    super(cause);
    this.command = null;
  }

  protected AbstractCommandException(final Throwable cause, final AbstractCommand command) {
    super(cause);
    this.command = command;
  }

  protected AbstractCommandException(final String message, final AbstractCommand command) {
    super(message);
    this.command = command;
  }
  
  protected AbstractCommandException(final String message, final Throwable cause) {
    super(message, cause);
    this.command = null;
  }
  
  protected AbstractCommandException(final String message, final Throwable cause, final AbstractCommand command) {
    super(message, cause);
    this.command = command;
  }

  public AbstractCommand getCommand() {
    return this.command;
  }
  
}