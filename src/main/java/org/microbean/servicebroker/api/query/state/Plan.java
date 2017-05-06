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
  
  public Plan(@NotNull final String id,
              @NotEmpty final String name,
              @NotEmpty final String description,
              final Map<@NotNull ? extends String, ?> metadata,
              final boolean free,
              final Boolean bindable) {
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
  
}

