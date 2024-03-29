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

package com.android.jack.load;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.util.location.Location;
import com.android.sched.util.location.NoLocation;

import javax.annotation.Nonnull;

/**
 * A {@link ClassOrInterfaceLoader} doing nothing.
 */
public class NopClassOrInterfaceLoader extends AbtractClassOrInterfaceLoader {

  @Nonnull
  private static final NoLocation NO_LOCATION = new NoLocation();

  @Nonnull
  public static final ClassOrInterfaceLoader INSTANCE = new NopClassOrInterfaceLoader();

  private NopClassOrInterfaceLoader() {
    // Nothing to do
  }

  @Override
  @Nonnull
  public Location getLocation(@Nonnull JDefinedClassOrInterface loaded) {
    return NO_LOCATION;
  }

  @Override
  protected void ensureAll(@Nonnull JDefinedClassOrInterface loaded) {
    // Nothing to do
  }
}