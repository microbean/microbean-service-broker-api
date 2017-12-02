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
package org.microbean.servicebroker.api;

import javax.validation.constraints.NotNull;

import org.microbean.servicebroker.api.query.LastOperationQuery;

import org.microbean.servicebroker.api.query.state.Catalog;
import org.microbean.servicebroker.api.query.state.LastOperation;

import org.microbean.servicebroker.api.command.DeleteBindingCommand;
import org.microbean.servicebroker.api.command.DeleteServiceInstanceCommand;
import org.microbean.servicebroker.api.command.ProvisionBindingCommand;
import org.microbean.servicebroker.api.command.ProvisionServiceInstanceCommand;
import org.microbean.servicebroker.api.command.UpdateServiceInstanceCommand;

public abstract class ServiceBroker {

  @NotNull
  public LastOperation getLastOperation(@NotNull final LastOperationQuery lastOperationQuery) throws ServiceBrokerException {
    throw new ServiceBrokerException(new UnsupportedOperationException());
  }

  public boolean isSupportedServiceId(final String serviceId) throws ServiceBrokerException {
    return serviceId != null;
  }

  public boolean isSupportedPlanId(final String serviceId, final String planId) throws ServiceBrokerException {
    return planId != null;
  }

  public abstract boolean isPlanBindable(final String serviceId, final String planId) throws ServiceBrokerException;
  
  public boolean isAsynchronousOnly() {
    return false;
  }
  
  @NotNull
  public abstract Catalog getCatalog() throws ServiceBrokerException;

  @NotNull
  public abstract ProvisionBindingCommand.Response execute(@NotNull final ProvisionBindingCommand command) throws ServiceBrokerException;

  @NotNull
  public abstract DeleteBindingCommand.Response execute(@NotNull final DeleteBindingCommand command) throws ServiceBrokerException;

  @NotNull
  public abstract ProvisionServiceInstanceCommand.Response execute(@NotNull final ProvisionServiceInstanceCommand command) throws ServiceBrokerException;

  @NotNull
  public abstract UpdateServiceInstanceCommand.Response execute(@NotNull final UpdateServiceInstanceCommand command) throws ServiceBrokerException;

  @NotNull
  public abstract DeleteServiceInstanceCommand.Response execute(@NotNull final DeleteServiceInstanceCommand command) throws ServiceBrokerException;

}
