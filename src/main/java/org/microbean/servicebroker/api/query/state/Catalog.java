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

import java.net.URI;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class Catalog {

  private Map<String, Object> properties;
  
  @NotNull
  private final Set<@NotNull ? extends Service> services;

  public Catalog() {
    this(Collections.emptySet());
  }
  
  public Catalog(final Set<@NotNull ? extends Service> services) {
    super();
    if (services == null || services.isEmpty()) {
      this.services = Collections.emptySet();
    } else {
      this.services = Collections.unmodifiableSet(new LinkedHashSet<>(services));
    }
    this.properties = new LinkedHashMap<>();
  }
  
  public Map<? extends String, ?> getProperties() {
    return Collections.unmodifiableMap(this.properties);
  }

  public void setProperty(final String name, final Object property) {
    this.properties.put(name, property);
  }

  @NotNull
  public final Set<@NotNull ? extends Service> getServices() {
    return this.services;
  }

  public static class Service {

    @NotNull
    private final String id;
  
    /**
     * From the specification: "A CLI-friendly name of the service. All
     * lowercase, no spaces.  This must be globally unique within a
     * platform marketplace."
     */
    @NotNull
    private final String name;

    @NotNull
    private final String description;

    private final Set<@NotNull ? extends String> tags;

    private final Set<@NotNull ? extends String> requires;

    private final boolean bindable;
  
    private final Map<@NotNull ? extends String, ?> metadata;

    private DashboardClient dashboardClient;

    /**
     * Note that the specification calls this {@code planUpdateable}
     * (note the misspelling).
     */
    private final boolean planUpdatable;

    @NotEmpty
    private final Set<@NotNull ? extends Plan> plans;

    public Service(@NotNull final String id, // GUID
                   @NotNull final String name,
                   @NotNull final String description,
                   final Set<@NotNull ? extends String> tags,
                   final Set<@NotNull ? extends String> requires,
                   final boolean bindable,
                   final Map<@NotNull ? extends String, ?> metadata,
                   final DashboardClient dashboardClient,
                   final boolean planUpdatable,
                   @NotEmpty final Set<@NotNull ? extends Plan> plans) {
      super();
      Objects.requireNonNull(id);
      Objects.requireNonNull(name);
      Objects.requireNonNull(description);
      Objects.requireNonNull(plans);    
      this.id = id;
      this.name = name;
      this.description = description;
      if (tags == null || tags.isEmpty()) {
        this.tags = Collections.emptySet();
      } else {
        this.tags = new LinkedHashSet<>(tags);
      }
      if (requires == null || requires.isEmpty()) {
        this.requires = Collections.emptySet();
      } else {
        this.requires = new LinkedHashSet<>(requires);
      }
      this.bindable = bindable;
      if (metadata == null || metadata.isEmpty()) {
        this.metadata = Collections.emptyMap();
      } else {
        this.metadata = new LinkedHashMap<>(metadata);
      }
      this.dashboardClient = dashboardClient;
      this.planUpdatable = planUpdatable;
      this.plans = Collections.unmodifiableSet(new LinkedHashSet<>(plans));
    }

    @NotNull
    public final String getId() {
      return this.id;
    }

    @NotNull
    public final String getName() {
      return this.name;
    }

    @NotNull
    public final String getDescription() {
      return this.description;
    }

    public final Set<@NotNull ? extends String> getTags() {
      return this.tags;
    }

    public final Set<@NotNull ? extends String> getRequires() {
      return this.requires;
    }

    public final boolean isBindable() {
      return this.bindable;
    }

    public final Map<? extends String, ?> getMetadata() {
      return this.metadata;
    }

    public final DashboardClient getDashboardClient() {
      return this.dashboardClient;
    }

    public boolean isPlanUpdatable() {
      return this.planUpdatable;
    }

    @NotEmpty
    public final Set<@NotNull ? extends Plan> getPlans() {
      return this.plans;
    }

    @Override
    public int hashCode() {
      int hashCode = 37;
      final Object id = this.getId();
      int c = id == null ? 0 : id.hashCode();
      hashCode = hashCode * 17 + c;
      return hashCode;
    }
    
    @Override
    public boolean equals(final Object other) {
      if (other == this) {
        return true;
      } else if (other instanceof Service) {
        final Service her = (Service)other;
        final Object id = this.getId();
        if (id == null) {
          if (her.getId() != null) {
            return false;
          }
        } else if (!id.equals(her.getId())) {
          return false;
        }
        return true;
      } else {
        return false;
      }
    }


    /*
     * Inner and nested classes.
     */
    

    public static class DashboardClient {

      private final String oAuthClientId;
      
      private final String secret;
      
      private final URI redirectUri;
      
      public DashboardClient(final String oAuthClientId,
                             final String secret,
                             final URI redirectUri) {
        super();
        this.oAuthClientId = oAuthClientId;
        this.secret = secret;
        this.redirectUri = redirectUri;
      }
      
      public final String getOAuthClientId() {
        return this.oAuthClientId;
      }
      
      public final String getSecret() {
        return this.secret;
      }
      
      public final URI getRedirectUri() {
        return this.redirectUri;
      }
      
    }
    
    public static class Plan {

      @NotNull
      private final String id;

      @NotEmpty
      private final String name;

      @NotEmpty
      private final String description;
  
      private final Map<@NotNull ? extends String, ?> metadata;
  
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
          this.metadata = Collections.unmodifiableMap(new LinkedHashMap<>(metadata));
        }
        this.free = free;
        this.bindable = bindable;
        this.schemas = schemas;
      }

      @NotNull
      public final String getId() {
        return this.id;
      }

      @NotNull
      public final String getName() {
        return this.name;
      }

      @NotNull
      public final String getDescription() {
        return this.description;
      }

      public final Map<@NotNull ? extends String, ?> getMetadata() {
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

        private final ServiceInstance serviceInstance;

        private final ServiceBinding serviceBinding;

        public Schema() {
          this(null, null);
        }
        
        public Schema(final ServiceInstance serviceInstance) {
          this(serviceInstance, null);
        }

        public Schema(final ServiceBinding serviceBinding) {
          this(null, serviceBinding);
        }
        
        public Schema(final ServiceInstance serviceInstance,
                      final ServiceBinding serviceBinding) {
          super();
          this.serviceInstance = serviceInstance;
          this.serviceBinding = serviceBinding;
        }

        public final ServiceInstance getServiceInstance() {
          return this.serviceInstance;
        }

        public final ServiceBinding getServiceBinding() {
          return this.serviceBinding;
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

          private final Map<@NotNull ? extends String, ?> parameters;

          public InputParameters(final Map<@NotNull ? extends String, ?> parameters) {
            super();
            this.parameters = parameters;
          }

          public final Map<@NotNull ? extends String, ?> getParameters() {
            return this.parameters;
          }
      
        }
    
      }
  
    }


      
  }
    
}
