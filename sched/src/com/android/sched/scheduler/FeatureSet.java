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

package com.android.sched.scheduler;

import com.android.sched.item.Feature;
import com.android.sched.item.ItemManager;
import com.android.sched.item.ItemSet;

import javax.annotation.Nonnull;

/**
 * An {@link ItemSet} specialized in {@link Feature} classes.
 */
public class FeatureSet extends ItemSet<Feature> {
  public FeatureSet(@Nonnull FeatureSet initial) {
    super(initial);
  }

  public FeatureSet(@Nonnull ItemManager manager) {
    super(manager);
  }

  @Nonnull
  @Override
  public FeatureSet clone() {
    return (FeatureSet) super.clone();
  }
}
