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

import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.microbean.servicebroker.api.query.state.Catalog;
import org.microbean.servicebroker.api.query.state.Catalog.Service;

import org.microbean.servicebroker.api.command.DeleteBindingCommand;
import org.microbean.servicebroker.api.command.DeleteServiceInstanceCommand;
import org.microbean.servicebroker.api.command.InvalidServiceBrokerCommandException;
import org.microbean.servicebroker.api.command.ProvisionBindingCommand;
import org.microbean.servicebroker.api.command.ProvisionServiceInstanceCommand;
import org.microbean.servicebroker.api.command.UpdateServiceInstanceCommand;

public abstract class CompositeServiceBroker extends ServiceBroker {

  private final Map<String, ServiceBroker> serviceBrokers;

  private final Map<String, ServiceBroker> serviceBrokersByServiceId;

  private final ReadWriteLock serviceBrokerAssociationLock;
  
  public CompositeServiceBroker() {
    super();
    this.serviceBrokerAssociationLock = new ReentrantReadWriteLock();
    this.serviceBrokers = new ConcurrentHashMap<>();
    this.serviceBrokersByServiceId = new HashMap<>();
  }
  
  public ServiceBroker putServiceBroker(@NotNull final String key, @NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(key);
    Objects.requireNonNull(serviceBroker);
    return this.serviceBrokers.put(key, serviceBroker);
  }

  public ServiceBroker removeServiceBroker(@NotNull final String key) {
    Objects.requireNonNull(key);
    return this.serviceBrokers.remove(key);
  }

  protected void clearServiceBrokerAssociations() {
    this.serviceBrokersByServiceId.clear();
  }
  
  protected ServiceBroker getServiceBrokerForServiceId(@NotEmpty final String serviceId) {
    Objects.requireNonNull(serviceId);
    return this.serviceBrokersByServiceId.get(serviceId);
  }

  protected ServiceBroker putServiceBrokerForServiceId(@NotEmpty final String serviceId, @NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceId);
    Objects.requireNonNull(serviceBroker);
    return this.serviceBrokersByServiceId.put(serviceId, serviceBroker);
  }

  protected Catalog getCatalog(@NotNull final ServiceBroker serviceBroker) throws ServiceBrokerException {
    Objects.requireNonNull(serviceBroker);
    return serviceBroker.getCatalog();
  }

  @NotNull
  @Override
  public Catalog getCatalog() throws ServiceBrokerException {
    Catalog returnValue = null;
    final Iterable<? extends Entry<? extends String, ? extends ServiceBroker>> serviceBrokersEntrySet = this.serviceBrokers.entrySet();
    if (serviceBrokersEntrySet != null) {
      final Set<Service> allServices = new HashSet<>();
      try {
        this.serviceBrokerAssociationLock.writeLock().lock();
        this.clearServiceBrokerAssociations();
        for (final Entry<? extends String, ? extends ServiceBroker> entry : serviceBrokersEntrySet) {
          if (entry != null) {
            final String key = entry.getKey();
            if (key != null) {
              final ServiceBroker serviceBroker = entry.getValue();
              if (serviceBroker != null) {
                final Catalog catalog = this.getCatalog(serviceBroker);
                if (catalog != null) {
                  final Iterable<? extends Service> services = catalog.getServices();
                  if (services != null) {
                    for (final Service service : services) {
                      if (service != null) {
                        final String serviceId = service.getId();
                        if (serviceId != null) {
                          this.putServiceBrokerForServiceId(serviceId, serviceBroker);
                          allServices.add(service);
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      } finally {
        this.serviceBrokerAssociationLock.writeLock().unlock();
      }
      if (!allServices.isEmpty()) {
        returnValue = new Catalog(allServices);
      }
    }
    if (returnValue == null) {
      returnValue = new Catalog();
    }
    return returnValue;
  }

  @NotNull
  public ProvisionBindingCommand.Response execute(@NotNull final ProvisionBindingCommand command) throws ServiceBrokerException {
    Objects.requireNonNull(command);
    ProvisionBindingCommand.Response returnValue = null;
    final String serviceId = command.getServiceId();
    if (serviceId != null) {
      ServiceBroker serviceBroker = null;
      try {
        this.serviceBrokerAssociationLock.readLock().lock();
        serviceBroker = this.getServiceBrokerForServiceId(serviceId);
      } finally {
        this.serviceBrokerAssociationLock.readLock().unlock();
      }
      if (serviceBroker != null) {
        returnValue = serviceBroker.execute(command);
      }
    }
    if (returnValue == null) {
      throw new InvalidServiceBrokerCommandException(command);
    }
    return returnValue;
  }

  @NotNull
  @Override
  public DeleteServiceInstanceCommand.Response execute(@NotNull final DeleteServiceInstanceCommand command) throws ServiceBrokerException {
    Objects.requireNonNull(command);
    DeleteServiceInstanceCommand.Response returnValue = null;
    final String serviceId = command.getServiceId();
    if (serviceId != null) {
      ServiceBroker serviceBroker = null;
      try {
        this.serviceBrokerAssociationLock.readLock().lock();
        serviceBroker = this.getServiceBrokerForServiceId(serviceId);
      } finally {
        this.serviceBrokerAssociationLock.readLock().unlock();
      }
      if (serviceBroker != null) {
        returnValue = serviceBroker.execute(command);
      }
    }
    if (returnValue == null) {
      throw new InvalidServiceBrokerCommandException(command);
    }
    return returnValue;
  }

  @NotNull
  @Override
  public ProvisionServiceInstanceCommand.Response execute(@NotNull final ProvisionServiceInstanceCommand command) throws ServiceBrokerException {
    Objects.requireNonNull(command);
    ProvisionServiceInstanceCommand.Response returnValue = null;
    final String serviceId = command.getServiceId();
    if (serviceId != null) {
      ServiceBroker serviceBroker = null;
      try {
        this.serviceBrokerAssociationLock.readLock().lock();
        serviceBroker = this.getServiceBrokerForServiceId(serviceId);
      } finally {
        this.serviceBrokerAssociationLock.readLock().unlock();
      }
      if (serviceBroker != null) {
        returnValue = serviceBroker.execute(command);
      }
    }
    if (returnValue == null) {
      throw new InvalidServiceBrokerCommandException(command);
    }
    return returnValue;
  }

  @NotNull
  @Override
  public UpdateServiceInstanceCommand.Response execute(@NotNull final UpdateServiceInstanceCommand command) throws ServiceBrokerException {
    Objects.requireNonNull(command);
    UpdateServiceInstanceCommand.Response returnValue = null;
    final String serviceId = command.getServiceId();
    if (serviceId != null) {
      ServiceBroker serviceBroker = null;
      try {
        this.serviceBrokerAssociationLock.readLock().lock();
        serviceBroker = this.getServiceBrokerForServiceId(serviceId);
      } finally {
        this.serviceBrokerAssociationLock.readLock().unlock();
      }
      if (serviceBroker != null) {
        returnValue = serviceBroker.execute(command);
      }
    }
    if (returnValue == null) {
      throw new InvalidServiceBrokerCommandException(command);
    }
    return returnValue;
  }


}
