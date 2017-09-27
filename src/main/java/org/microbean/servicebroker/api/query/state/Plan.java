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
package org.microbean.servicebroker.api.query.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class Plan {
    
  private final String id;
  
  private final String name;
  
  private final String description;
  
  private final Map<? extends String, ?> metadata;
  
  private final boolean free;
  
  private final Boolean bindable;

  /**
   * The {@link Schema} object, singular, associated with this {@link
   * Plan}.
   *
   * <p>The documentation for the 2.13 release of the Open Service
   * Broker Specification is quite loose with singular and plural
   * terms.</p>
   *
   * @see <a
   * href="https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#plan-object">Plan
   * object documentation</a>
   *
   * @see <a
   * href="https://github.com/openservicebrokerapi/servicebroker/blob/v2.13/spec.md#schema-object">Schema
   * object documentation</a>
   */
  private final Schema schemas;
  
  public Plan(@NotNull final String id,
              @NotEmpty final String name,
              @NotEmpty final String description,
              final Map<@NotNull ? extends String, ?> metadata,
              final boolean free,
              final Boolean bindable) {
    this(id, name, description, metadata, free, bindable, null);
  }

  public Plan(@NotNull final String id,
              @NotEmpty final String name,
              @NotEmpty final String description,
              final Map<@NotNull ? extends String, ?> metadata,
              final boolean free,
              final Boolean bindable,
              final Schema schemas) {
    super();
    Objects.requireNonNull(id);
    Objects.requireNonNull(name);
    Objects.requireNonNull(description);
    this.id = id;
    this.name = name;
    this.description = description;
    if (metadata == null || metadata.isEmpty()) {
      this.metadata = Collections.emptyMap();
    } else {
      this.metadata = Collections.unmodifiableMap(new HashMap<>(metadata));
    }
    this.free = free;
    this.bindable = bindable;
    this.schemas = schemas;
  }

  public final String getId() {
    return this.id;
  }

  public final String getName() {
    return this.name;
  }

  public final String getDescription() {
    return this.description;
  }

  public final Map<? extends String, ?> getMetadata() {
    return this.metadata;
  }

  public final boolean isFree() {
    return this.free;
  }

  public final Boolean getBindable() {
    return this.bindable;
  }

  public final Schema getSchemas() {
    // The singular/plural mismatch is a property of the actual
    // specification.  :-(
    return this.schemas;
  }


  /*
   * Inner and nested classes.
   */

  
  public static class Schema {
    
    public Schema(final ServiceInstance serviceInstance,
                  final ServiceBinding serviceBinding) {
      super();
    }


    /*
     * Inner and nested classes.
     */

    
    public static class ServiceInstance {

      private final InputParameters create;

      private final InputParameters update;
      
      public ServiceInstance(final InputParameters create,
                             final InputParameters update) {
        super();
        this.create = create;
        this.update = update;
      }

      public InputParameters getCreate() {
        return this.create;
      }

      public InputParameters getUpdate() {
        return this.update;
      }
      
    }
    
    public static class ServiceBinding {

      private final InputParameters create;
      
      public ServiceBinding(final InputParameters create) {
        super();
        this.create = create;
      }

      public InputParameters getCreate() {
        return this.create;
      }
      
    }

    public static class InputParameters {

      private final Map<? extends String, ?> parameters;

      public InputParameters(final Map<? extends String, ?> parameters) {
        super();
        this.parameters = parameters;
      }

      public final Map<? extends String, ?> getParameters() {
        return this.parameters;
      }
      
    }
    
  }
  
}

