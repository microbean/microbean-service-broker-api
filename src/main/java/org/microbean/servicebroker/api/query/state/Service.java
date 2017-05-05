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
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

public class Service {

  private final String id;
  
  /**
   * From the specification: "A CLI-friendly name of the service. All
   * lowercase, no spaces.  This must be globally unique within a
   * platform marketplace."
   */
  private final String name;

  private final String description;

  private final Set<? extends String> tags;

  private final Set<? extends String> requires;

  private final boolean bindable;
  
  private final Map<? extends String, ?> metadata;

  private DashboardClient dashboardClient;

  /**
   * Note that the specification calls this {@code planUpdateable}
   * (note the misspelling).
   */
  private final boolean planUpdatable;

  private final Set<? extends Plan> plans;

  public Service(@NotNull final String id,
                 @NotNull final String name,
                 @NotNull final String description,
                 final Set<@NotNull ? extends String> tags,
                 final Set<@NotNull ? extends String> requires,
                 final boolean bindable,
                 final Map<@NotNull ? extends String, ?> metadata,
                 final DashboardClient dashboardClient,
                 final boolean planUpdatable,
                 @NotEmpty final Set<? extends Plan> plans) {
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
      this.tags = new HashSet<>(tags);
    }
    if (requires == null || requires.isEmpty()) {
      this.requires = Collections.emptySet();
    } else {
      this.requires = new HashSet<>(requires);
    }
    this.bindable = bindable;
    if (metadata == null || metadata.isEmpty()) {
      this.metadata = Collections.emptyMap();
    } else {
      this.metadata = new HashMap<>(metadata);
    }
    this.dashboardClient = dashboardClient;
    this.planUpdatable = planUpdatable;
    this.plans = Collections.unmodifiableSet(new HashSet<>(plans));
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

  public final Set<? extends String> getTags() {
    return this.tags;
  }

  public final Set<? extends String> getRequires() {
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

  public final Set<? extends Plan> getPlans() {
    return this.plans;
  }

}
