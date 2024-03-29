/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.shrob.shrink;

import com.android.jack.analysis.tracer.ExtendingOrImplementingClassMarker;
import com.android.jack.analysis.tracer.Tracer;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Use;

import javax.annotation.Nonnull;

/**
 * A {@link RunnableSchedulable} that marks all classes and members that will be kept when
 * shrinking.
 */
@Description("Marks all classes and members that will be kept when shrinking.")
@Constraint(need = {ExtendingOrImplementingClassMarker.class})
@Use(KeeperBrush.class)
public class Keeper implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private final Tracer tracer = new Tracer(new KeeperBrush());

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    tracer.run(type);
  }
}
