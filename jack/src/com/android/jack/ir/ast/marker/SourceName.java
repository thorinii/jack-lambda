/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack.ir.ast.marker;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVariable;
import com.android.sched.item.Description;
import com.android.sched.marker.Marker;
import com.android.sched.marker.ValidOn;

import javax.annotation.Nonnull;

/**
 * This {@link Marker} holds source name retrieved from ecj.
 */
@Description("Holds source name retrieved from ecj")
@ValidOn({JDefinedClassOrInterface.class, JVariable.class, JMethod.class, JField.class})
public class SourceName implements Marker {

  @Nonnull
  private final String sourceName;

  public SourceName(@Nonnull String sourceName) {
    this.sourceName = sourceName;
  }

  @Nonnull
  public String getSourceName() {
    return sourceName;
  }

  @Override
  public Marker cloneIfNeeded() {
    return this;
  }
}