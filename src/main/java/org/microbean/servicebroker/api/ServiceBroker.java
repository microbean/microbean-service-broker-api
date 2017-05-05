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

import org.microbean.servicebroker.api.query.state.Catalog;
import org.microbean.servicebroker.api.query.state.ServiceInstance;

import org.microbean.servicebroker.api.command.DeleteBindingCommand;
import org.microbean.servicebroker.api.command.DeleteServiceInstanceCommand;
import org.microbean.servicebroker.api.command.ProvisionBindingCommand;
import org.microbean.servicebroker.api.command.ProvisionServiceInstanceCommand;
import org.microbean.servicebroker.api.command.UpdateServiceInstanceCommand;

public abstract class ServiceBroker {

  public abstract Catalog getCatalog() throws ServiceBrokerException;
  
  public abstract ProvisionBindingCommand.Response execute(final ProvisionBindingCommand command) throws ServiceBrokerException;

  public abstract DeleteBindingCommand.Response execute(final DeleteBindingCommand command) throws ServiceBrokerException;

  public abstract ProvisionServiceInstanceCommand.Response execute(final ProvisionServiceInstanceCommand command) throws ServiceBrokerException;

  public abstract UpdateServiceInstanceCommand.Response execute(final UpdateServiceInstanceCommand command) throws ServiceBrokerException;

  public abstract DeleteServiceInstanceCommand.Response execute(final DeleteServiceInstanceCommand command) throws ServiceBrokerException;

  public abstract ServiceInstance getServiceInstance(final String instanceId) throws ServiceBrokerException;
  
}
