/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.jack.shrob.seed;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.shrob.spec.KeepModifier;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * A {@link Marker} that indicates that a node is a seed.
 */
@ValidOn(value = {JDefinedClassOrInterface.class, JField.class, JMethod.class})
@Description("Indicates that a node is a seed")
public class SeedMarker implements Marker {
  @Nonnull
  private KeepModifier modifier;

  public SeedMarker(@Nonnull KeepModifier modifier) {
    this.modifier = modifier;
  }

  @Nonnull
  public KeepModifier getModifier() {
    return modifier;
  }

  public void setModifier(@Nonnull KeepModifier modifier) {
    this.modifier = modifier;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}
