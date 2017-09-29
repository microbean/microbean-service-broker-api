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

import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import java.util.stream.Stream;

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

  private final Set<ServiceBroker> serviceBrokers;

  private final Map<String, ServiceBroker> serviceBrokersByServiceId;

  private final ReadWriteLock serviceBrokersLock;

  private final ReadWriteLock serviceBrokerAssociationLock;

  private boolean parallelServiceDiscovery;
  
  public CompositeServiceBroker() {
    super();
    this.serviceBrokersLock = new ReentrantReadWriteLock();
    this.serviceBrokerAssociationLock = new ReentrantReadWriteLock();
    this.serviceBrokers = new HashSet<>();
    this.serviceBrokersByServiceId = new HashMap<>();
  }

  public boolean getParallelServiceDiscovery() {
    return this.parallelServiceDiscovery;
  }

  public void setParallelServiceDiscovery(final boolean parallelServiceDiscovery) {
    this.parallelServiceDiscovery = parallelServiceDiscovery;
  }
  
  public final boolean addServiceBroker(@NotNull final ServiceBroker serviceBroker) {
    try {
      this.serviceBrokersLock.writeLock().lock();
      return this.handleAddServiceBroker(serviceBroker);
    } finally {
      this.serviceBrokersLock.writeLock().unlock();
    }
  }

  protected boolean handleAddServiceBroker(@NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceBroker);
    return this.serviceBrokers.add(serviceBroker);
  }

  public final boolean removeServiceBroker(@NotNull final ServiceBroker serviceBroker) {
    try {
      this.serviceBrokersLock.writeLock().lock();
      return this.handleRemoveServiceBroker(serviceBroker);
    } finally {
      this.serviceBrokersLock.writeLock().unlock();
    }
  }

  protected boolean handleRemoveServiceBroker(@NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceBroker);
    return this.serviceBrokers.remove(serviceBroker);
  }
  
  public final Set<ServiceBroker> getServiceBrokers() {
    try {
      this.serviceBrokersLock.readLock().lock();
      return this.handleGetServiceBrokers();
    } finally {
      this.serviceBrokersLock.readLock().unlock();
    }
  }

  /**
   * Returns a {@link Set} of {@link ServiceBroker}s that semantically
   * was built up as a result of {@link
   * #addServiceBroker(ServiceBroker)} and {@link
   * #removeServiceBroker(ServiceBroker)} operations.
   *
   * <p>Implementations of this method may return {@code null}.</p>
   *
   * <p>Modifications to the {@link Set} returned by this method must
   * not be made by any mechanism other than that implemented by the
   * {@link #handleRemoveServiceBroker(ServiceBroker)} and {@link
   * #handleAddServiceBroker(ServiceBroker)} methods, or a {@link
   * ConcurrentModificationException} may be thrown by the default
   * implementation of the {@link #getCatalog()} method.</p>
   *
   * @return a {@link Set} of {@link ServiceBroker} instances, or
   * {@code null}
   *
   * @see #getServiceBrokers()
   *
   * @see #getCatalog()
   */
  protected Set<ServiceBroker> handleGetServiceBrokers() {
    return this.serviceBrokers;
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

  @NotNull
  protected Catalog getCatalog(@NotNull final ServiceBroker serviceBroker) throws ServiceBrokerException {
    Objects.requireNonNull(serviceBroker);
    return serviceBroker.getCatalog();
  }

  @NotNull
  @Override
  public final Catalog getCatalog() throws ServiceBrokerException {
    Catalog returnValue = null;
    Set<ServiceBroker> serviceBrokers = null;
    try {
      this.serviceBrokersLock.readLock().lock();
      serviceBrokers = this.getServiceBrokers();
      if (serviceBrokers == null || serviceBrokers.isEmpty()) {
        serviceBrokers = Collections.emptySet();
      } else {
        serviceBrokers = new HashSet<>(serviceBrokers);
      }
    } finally {
      this.serviceBrokersLock.readLock().unlock();
    }
    assert serviceBrokers != null;
    if (!serviceBrokers.isEmpty()) {
      final Set<Service> allServices = new HashSet<>();
      try {
        this.serviceBrokerAssociationLock.writeLock().lock();
        this.clearServiceBrokerAssociations();
        final Map<Service, ServiceBroker> associations = new ConcurrentHashMap<>();
        final Stream<ServiceBroker> serviceBrokerStream;
        if (this.getParallelServiceDiscovery()) {
          serviceBrokerStream = serviceBrokers.parallelStream();
        } else {
          serviceBrokerStream = serviceBrokers.stream();
        }        
        try {
          serviceBrokerStream
            .forEach(serviceBroker -> {
                if (serviceBroker != null) {
                  Catalog catalog = null;
                  try {
                    catalog = this.getCatalog(serviceBroker);
                  } catch (final ServiceBrokerException serviceBrokerException) {
                    throw new IllegalStateException(serviceBrokerException);
                  }
                  if (catalog != null) {
                    final Iterable<? extends Service> services = catalog.getServices();
                    if (services != null) {
                      for (final Service service : services) {
                        if (service != null) {
                          associations.put(service, serviceBroker);
                        }
                      }
                    }
                  }
                }
              });
        } catch (final IllegalStateException illegalStateException) {
          final Throwable cause = illegalStateException.getCause();
          if (cause instanceof ServiceBrokerException) {
            throw (ServiceBrokerException)cause;
          }
          throw illegalStateException;
        }
        associations.entrySet().stream()
          .forEach(entry -> {
              assert entry != null;
              final Service service = entry.getKey();
              assert service != null;
              final String id = service.getId();
              if (id != null) {
                final ServiceBroker serviceBroker = entry.getValue();
                assert serviceBroker != null;
                this.putServiceBrokerForServiceId(id, serviceBroker);
              }
            });
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
