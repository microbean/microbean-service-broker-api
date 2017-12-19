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

import java.util.Objects;

import javax.validation.constraints.NotNull;

import org.microbean.servicebroker.api.AbstractStatefulObject;

public class LastOperation extends AbstractStatefulObject {

  private final State state;

  private final String description;

  public LastOperation(@NotNull final State state) {
    this(state, null);
  }
  
  public LastOperation(@NotNull final State state, final String description) {
    super();
    Objects.requireNonNull(state, () -> "state must not be null");
    this.state = state;
    this.description = description;
  }

  public final State getState() {
    return this.state;
  }

  public final String getDescription() {
    return this.description;
  }


  /*
   * Inner and nested classes.
   */

  
  public static enum State {
    IN_PROGRESS("in progress"),
    SUCCEEDED("succeeded"),
    FAILED("failed");

    private final String value;
    
    State(final String value) {
      Objects.requireNonNull(value, () -> "value must not be null");
      this.value = value;
    }

    @Override
    public final String toString() {
      return value;
    }

  }
  
}
