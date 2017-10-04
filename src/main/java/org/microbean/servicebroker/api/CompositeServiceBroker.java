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

import org.microbean.servicebroker.api.query.InvalidServiceBrokerQueryException;
import org.microbean.servicebroker.api.query.LastOperationQuery;

import org.microbean.servicebroker.api.query.state.Catalog;
import org.microbean.servicebroker.api.query.state.Catalog.Service;
import org.microbean.servicebroker.api.query.state.LastOperation;

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
  

  /**
   * Returns {@code true} if the {@link #getCatalog()} method's
   * internals should try to use parallel semantics when calling
   * {@link ServiceBroker#getCatalog() getCatalog()} on {@linkplain
   * #getServiceBrokers() all the <code>ServiceBroker</code>s} this
   * {@link CompositeServiceBroker} multiplexes.
   *
   * @return {@code true} if the {@link #getCatalog()} method's
   * internals should try to use parallel semantics when calling
   * {@link ServiceBroker#getCatalog() getCatalog()} on {@linkplain
   * #getServiceBrokers() all the <code>ServiceBroker</code>s} this
   * {@link CompositeServiceBroker} multiplexes; {@code false}
   * otherwise
   *
   * @see #setParallelServiceDiscovery(boolean)
   *
   * @see #getCatalog()
   */
  public boolean getParallelServiceDiscovery() {
    return this.parallelServiceDiscovery;
  }

  /**
   * Sets whether the {@link #getCatalog()} method's internals should
   * try to use parallel semantics when calling {@link
   * ServiceBroker#getCatalog() getCatalog()} on {@linkplain
   * #getServiceBrokers() all the <code>ServiceBroker</code>s} this
   * {@link CompositeServiceBroker} multiplexes.
   *
   * @param parallelServiceDiscovery if {@code true}, then the {@link
   * #getCatalog()} method's internals will try to use parallel
   * semantics when calling {@link ServiceBroker#getCatalog()
   * getCatalog()} on {@linkplain #getServiceBrokers() all the
   * <code>ServiceBroker</code>s} this {@link CompositeServiceBroker}
   * multiplexes
   *
   * @see #getParallelServiceDiscovery()
   *
   * @see #getCatalog()
   */
  public void setParallelServiceDiscovery(final boolean parallelServiceDiscovery) {
    this.parallelServiceDiscovery = parallelServiceDiscovery;
  }

  /**
   * Calls the {@link #handleAddServiceBroker(ServiceBroker)} method
   * supplying it the supplied {@code serviceBroker} and returns the
   * result.
   *
   * <p>This method acquires and properly releases a write lock
   * internally that ensures that overrides of the {@link
   * #handleAddServiceBroker(ServiceBroker)} method do not have to
   * worry about concurrency concerns, particularly while a {@link
   * #getCatalog()} invocation is in process.</p>
   *
   * @param serviceBroker the {@link ServiceBroker} to add; must not
   * be {@code null}
   *
   * @return {@code true} if the supplied {@link ServiceBroker} was
   * added; {@code false} otherwise
   *
   * @exception NullPointerException if {@code serviceBroker} was
   * {@code null}
   *
   * @see #handleAddServiceBroker(ServiceBroker)
   */
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

  /**
   * Calls the {@link #handleRemoveServiceBroker(ServiceBroker)} method
   * supplying it the supplied {@code serviceBroker} and returns the
   * result.
   *
   * <p>This method first calls the {@link
   * #removeServices(ServiceBroker)} method supplying
   * it the {@link ServiceBroker} that has just been removed, also
   * with proper concurrency semantics.</p>
   *
   * <p>This method acquires and properly releases a write lock
   * internally that ensures that overrides of the {@link
   * #handleRemoveServiceBroker(ServiceBroker)} method do not have to
   * worry about concurrency concerns, particularly while a {@link
   * #getCatalog()} invocation is in process.</p>
   *
   * @param serviceBroker the {@link ServiceBroker} to remove; must not
   * be {@code null}
   *
   * @return {@code true} if the supplied {@link ServiceBroker} was
   * removeed; {@code false} otherwise
   *
   * @exception NullPointerException if {@code serviceBroker} was
   * {@code null}
   *
   * @see #handleRemoveServiceBroker(ServiceBroker)
   *
   * @see #removeServices(ServiceBroker)
   */
  public final boolean removeServiceBroker(@NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceBroker);    
    if (serviceBroker == this) {
      throw new IllegalArgumentException("serviceBroker == this");
    }
    try {
      this.serviceBrokersLock.writeLock().lock();
      try {
        this.serviceBrokerAssociationLock.writeLock().lock();
        this.removeServices(serviceBroker);
      } finally {
        this.serviceBrokerAssociationLock.writeLock().unlock();
      }
      return this.handleRemoveServiceBroker(serviceBroker);
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
   * <p>This method is called by the {@link #getServiceBrokers()}
   * method.  Calling this method in any other fashion may result in
   * undefined behavior.</p>
   *
   * <p>Overrides of this method must not call the {@link
   * #getServiceBrokers()} method or a {@link StackOverflowError} may
   * result.</p>
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

  /**
   * Effectively removes all {@link Service}s from this {@link
   * CompositeServiceBroker} during a {@link #getCatalog()} operation
   * while they are being recalculated.
   *
   * <p>This method is called by the {@link #getCatalog()} method.
   * Calling this method in any other fashion may result in undefined
   * behavior.</p>
   *
   * <p>Overrides of this method must not call the {@link
   * #getCatalog()} method or a {@link StackOverflowError} may
   * result.</p>
   *
   * @see #removeServices(ServiceBroker)
   */
  protected void removeServices() {
    this.serviceBrokersByServiceId.clear();
  }

  /**
   * Effectively emoves all {@link Service}s associated with the
   * supplied {@link ServiceBroker} from being visible or known about
   * in any way by this {@link CompositeServiceBroker} during an
   * invocation of the {@link #removeServiceBroker(ServiceBroker)}
   * method immediately before the supplied {@link ServiceBroker} is
   * removed from this {@link CompositeServiceBroker} by the {@link
   * #handleRemoveServiceBroker(ServiceBroker)} method.
   *
   * <p>Overrides of this method must not call the following methods,
   * or undefined behavior may result:</p>
   *
   * <ul>
   *
   * <li>{@link #removeServiceBroker(ServiceBroker)}</li>
   *
   * <li>{@link #handleAddServiceBroker(ServiceBroker)}</li>
   *
   * <li>{@link #handleRemoveServiceBroker(ServiceBroker)}</li>
   *
   * <li>{@link #putServiceBrokerForServiceId(String,
   * ServiceBroker)}</li>
   *
   * </ul>
   *
   * @param serviceBroker the {@link ServiceBroker} whose logical
   * {@link Service} entries should be logically removed from this
   * {@link CompositeServiceBroker}; must not be {@code null}
   *
   * @exception NullPointerException if {@code serviceBroker} is {@code null}
   *
   * @exception IllegalArgumentException if {@code serviceBroker} is
   * this {@link CompositeServiceBroker}
   */
  protected void removeServices(@NotNull final ServiceBroker serviceBroker) {
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

  /**
   * Returns the {@link ServiceBroker} logically responsible for the
   * logical {@link Service} {@linkplain Service#getId() identified
   * by} the supplied {@code serviceId}, or {@code null} if no such
   * {@link ServiceBroker} exists.
   *
   * <p>This method may return {@code null}.</p>
   *
   * <p>Overrides of this method may return {@code null}.</p>
   *
   * @param serviceId an {@linkplain Service#getId() identifier} of a
   * {@link Service}; must not be {@code null}
   *
   * @return the {@link ServiceBroker} logically responsible for the
   * logical {@link Service} {@linkplain Service#getId() identified
   * by} the supplied {@code serviceId}, or {@code null}
   *
   * @exception NullPointerException if {@code serviceId} is {@code
   * null}
   */
  protected ServiceBroker getServiceBrokerForServiceId(@NotEmpty final String serviceId) {
    Objects.requireNonNull(serviceId);
    return this.serviceBrokersByServiceId.get(serviceId);
  }

  /**
   * Records an association between a {@link Service} (represented
   * here by the {@link Service#getId() serviceId} parameter) and the
   * {@link ServiceBroker} responsible for it, and returns any {@link
   * ServiceBroker} previously responsible for the association.
   *
   * <p>This method may return {@code null} (and in fact should).</p>
   *
   * <p>Overrides of this method may return {@code null} (and in fact
   * should).</p>
   *
   * @param serviceId the {@linkplain Service#getId() identifier of a
   * <code>Service</code>}; must not be {@code null}
   *
   * @param serviceBroker the {@link ServiceBroker} that is
   * responsible for the {@link Service} whose {@linkplain
   * Service#getId() identifier} is the value of the {@code serviceId}
   * parameter; must not be {@code null}
   *
   * @return the {@link ServiceBroker} that was previously linked;
   * {@code null} in most if not all cases
   *
   * @exception NullPointerException if either {@code serviceId} or
   * {@code serviceBroker} is {@code null}
   *
   * @exception IllegalArgumentException if {@code serviceBroker} is
   * this very {@link CompositeServiceBroker}
   */
  protected ServiceBroker putServiceBrokerForServiceId(@NotEmpty final String serviceId, @NotNull final ServiceBroker serviceBroker) {
    Objects.requireNonNull(serviceId);
    Objects.requireNonNull(serviceBroker);
    if (serviceBroker == this) {
      throw new IllegalArgumentException("serviceBroker == this");
    }
    return this.serviceBrokersByServiceId.put(serviceId, serviceBroker);
  }

  /**
   * Returns a {@link Catalog} for the given {@link ServiceBroker}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>Overrides of this method must not return {@code null}.</p>
   *
   * <p>The default implementation of this method simply calls the
   * {@link ServiceBroker#getCatalog()} method on the supplied {@code
   * serviceBroker} and returns it.  Overrides may wish to do
   * additional processing or logging of this call.</p>
   *
   * @param serviceBroker the {@link ServiceBroker} whose {@linkplain
   * ServiceBroker#getCatalog() catalog} should be returned; must not
   * be {@code null}
   *
   * @return a non-{@code null} {@link Catalog}, normally as a result
   * of calling the {@link ServiceBroker#getCatalog()} method on the
   * supplied {@link ServiceBroker}
   *
   * @exception NullPointerException if {@code serviceBroker} is
   * {@code null}
   *
   * @exception IllegalArgumentException if {@code serviceBroker} is
   * this very {@link CompositeServiceBroker}
   *
   * @exception ServiceBrokerException if any other error occurs
   *
   * @see ServiceBroker#getCatalog()
   */
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
  public final LastOperation getLastOperation(final LastOperationQuery lastOperationQuery) throws ServiceBrokerException {
    Objects.requireNonNull(lastOperationQuery);    
    LastOperation returnValue = null;
    final String serviceId = lastOperationQuery.getServiceId();
    if (serviceId != null) {
      ServiceBroker serviceBroker = null;
      try {
        this.serviceBrokerAssociationLock.readLock().lock();
        serviceBroker = this.getServiceBrokerForServiceId(serviceId);
      } finally {
        this.serviceBrokerAssociationLock.readLock().unlock();
      }
      if (serviceBroker != null) {
        returnValue = serviceBroker.getLastOperation(lastOperationQuery);
      }
    }
    if (returnValue == null) {
      throw new InvalidServiceBrokerQueryException(lastOperationQuery);
    }
    return returnValue;
  }

  /**
   * Returns a {@link Catalog} that represents the logical union of
   * all the {@link Catalog} instances {@linkplain
   * ServiceBroker#getCatalog() from all the
   * <code>ServiceBroker</code>s} that are multiplexed by this {@link
   * CompositeServiceBroker}.
   *
   * <p>This method never returns {@code null}.</p>
   *
   * <p>This method is safe for concurrent use by multiple
   * threads.</p>
   *
   * <p>The implementation of this method is governed by the return
   * value of the {@link #getParallelServiceDiscovery()} method: if
   * {@code true}, then the {@link ServiceBroker}s that this {@link
   * CompositeServiceBroker} multiplexes are consulted for their
   * {@link Catalog} instances in parallel.</p>
   *
   * <p>Throughout the implementation of this method, appropriate
   * locking semantics are used such that overrides of the various
   * {@code protected} methods in this class do not have to
   * synchronize or otherwise guard against concurrent access.</p>
   *
   * @return a non-{@code null} {@link Catalog}
   *
   * @exception ServiceBrokerException if an error occurs
   *
   * @see #getServiceBrokers()
   *
   * @see #getParallelServiceDiscovery()
   *
   * @see #getCatalog(ServiceBroker)
   *
   * @see #putServiceBrokerForServiceId(String, ServiceBroker)
   *
   * @see #removeServices()
   */
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
        this.removeServices();
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
