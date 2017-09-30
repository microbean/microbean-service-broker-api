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
import java.util.Iterator;
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

/**
 * A {@link ServiceBroker} that multiplexes other {@link
 * ServiceBroker}s.
 *
 * @author <a href="https://about.me/lairdnelson"
 * target="_parent">Laird Nelson</a>
 *
 * @see ServiceBroker
 */
public class CompositeServiceBroker extends ServiceBroker {


  /*
   * Instance variables.
   */


  /**
   * A {@link Set} of {@link ServiceBroker}s that this {@link
   * CompositeServiceBroker} multiplexes.
   *
   * <p>This field is never {@code null}.</p>
   *
   * @see #handleAddServiceBroker(ServiceBroker)
   *
   * @see #handleRemoveServiceBroker(ServiceBroker)
   */
  @NotNull
  private final Set<ServiceBroker> serviceBrokers;

  @NotNull
  private final Map<String, ServiceBroker> serviceBrokersByServiceId;

  @NotNull
  private final ReadWriteLock serviceBrokersLock;

  @NotNull
  private final ReadWriteLock serviceBrokerAssociationLock;

  private boolean parallelServiceDiscovery;


  /*
   * Constructors.
   */

  public CompositeServiceBroker() {
    this(null, false);
  }
  
  public CompositeServiceBroker(final boolean parallelServiceDiscovery) {
    this(null, parallelServiceDiscovery);
  }
  
  public CompositeServiceBroker(final Set<? extends ServiceBroker> serviceBrokers) {
    this(serviceBrokers, false);
  }
  
  public CompositeServiceBroker(final Set<? extends ServiceBroker> serviceBrokers, final boolean parallelServiceDiscovery) {
    super();
    this.serviceBrokersLock = new ReentrantReadWriteLock();
    this.serviceBrokerAssociationLock = new ReentrantReadWriteLock();
    this.parallelServiceDiscovery = parallelServiceDiscovery;
    this.serviceBrokers = new HashSet<>();
    this.serviceBrokersByServiceId = new HashMap<>();
    if (serviceBrokers != null) {
      for (final ServiceBroker serviceBroker : serviceBrokers) {
        if (serviceBroker != null) {
          this.addServiceBroker(serviceBroker);
        }
      }
    }
  }


  /*
   * Instance methods.
   */
  

  public boolean getParallelServiceDiscovery() {
    return this.parallelServiceDiscovery;
  }

  public void setParallelServiceDiscovery(final boolean parallelServiceDiscovery) {
    this.parallelServiceDiscovery = parallelServiceDiscovery;
  }
  
  public final boolean addServiceBroker(@NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceBroker);
    if (serviceBroker == this) {
      throw new IllegalArgumentException("serviceBroker == this");
    }
    try {
      this.serviceBrokersLock.writeLock().lock();
      return this.handleAddServiceBroker(serviceBroker);
    } finally {
      this.serviceBrokersLock.writeLock().unlock();
    }
  }

  protected boolean handleAddServiceBroker(@NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceBroker);
    if (serviceBroker == this) {
      throw new IllegalArgumentException("serviceBroker == this");
    }
    return this.serviceBrokers.add(serviceBroker);
  }

  public final boolean removeServiceBroker(@NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceBroker);    
    if (serviceBroker == this) {
      throw new IllegalArgumentException("serviceBroker == this");
    }
    try {
      this.serviceBrokersLock.writeLock().lock();
      final boolean returnValue = this.handleRemoveServiceBroker(serviceBroker);
      try {
        this.serviceBrokerAssociationLock.writeLock().lock();
        this.clearServiceBrokerAssociations(serviceBroker);
      } finally {
        this.serviceBrokerAssociationLock.writeLock().unlock();
      }
      return returnValue;
    } finally {
      this.serviceBrokersLock.writeLock().unlock();
    }
  }

  protected boolean handleRemoveServiceBroker(@NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceBroker);
    if (serviceBroker == this) {
      throw new IllegalArgumentException("serviceBroker == this");
    }
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

  protected void clearServiceBrokerAssociations(@NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceBroker);
    if (serviceBroker == this) {
      throw new IllegalArgumentException("serviceBroker == this");
    }
    final Iterable<Entry<String, ServiceBroker>> entrySet = this.serviceBrokersByServiceId.entrySet();
    if (entrySet != null) {
      final Iterator<Entry<String, ServiceBroker>> iterator = entrySet.iterator();
      if (iterator != null && iterator.hasNext()) {
        while (iterator.hasNext()) {
          final Entry<String, ServiceBroker> entry = iterator.next();
          if (entry != null && serviceBroker.equals(entry.getValue())) {
            iterator.remove();
          }
        }
      }
    }
      
  }
  
  protected ServiceBroker getServiceBrokerForServiceId(@NotEmpty final String serviceId) {
    Objects.requireNonNull(serviceId);
    return this.serviceBrokersByServiceId.get(serviceId);
  }

  protected ServiceBroker putServiceBrokerForServiceId(@NotEmpty final String serviceId, @NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceId);
    Objects.requireNonNull(serviceBroker);
    if (serviceBroker == this) {
      throw new IllegalArgumentException("serviceBroker == this");
    }
    return this.serviceBrokersByServiceId.put(serviceId, serviceBroker);
  }

  @NotNull
  protected Catalog getCatalog(@NotNull final ServiceBroker serviceBroker) throws ServiceBrokerException {
    Objects.requireNonNull(serviceBroker);
    if (serviceBroker == this) {
      throw new IllegalArgumentException("serviceBroker == this");
    }
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
  public DeleteBindingCommand.Response execute(@NotNull final DeleteBindingCommand command) throws ServiceBrokerException {
    Objects.requireNonNull(command);
    DeleteBindingCommand.Response returnValue = null;
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

  /**
   * Invokes the {@link System#identityHashCode(Object)} method with
   * {@code this} as its argument and returns the result.
   *
   * @return a hashcode for this {@link CompositeServiceBroker}
   *
   * @see System#identityHashCode(Object)
   */
  @Override
  public final int hashCode() {
    return System.identityHashCode(this);
  }

  /**
   * Returns {@code true} if the supplied {@link Object} is this very
   * {@link CompositeServiceBroker} instance.
   *
   * @param other the {@link Object} to test; may be {@code null} in
   * which case {@code false} will be returned
   *
   * @return {@code true} if the supplied {@link Object} is this very
   * {@link CompositeServiceBroker} instance; {@code false} otherwise
   */
  @Override
  public final boolean equals(final Object other) {
    return this == other;
  }

}
