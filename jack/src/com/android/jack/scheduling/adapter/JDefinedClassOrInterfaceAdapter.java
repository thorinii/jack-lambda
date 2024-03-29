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

package com.android.jack.scheduling.adapter;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.sched.item.Description;
import com.android.sched.schedulable.AdapterSchedulable;

import java.util.ArrayList;
import java.util.Iterator;

import javax.annotation.Nonnull;

/**
 * Adapts a process on {@code JSession} onto one or several processes on each
 * {@code JDefinedClassOrInterface} to emit during this session.
 */
@Description("Adapts process on JSession to one or several processes on each of its " +
  "JDefinedClassOrInterface")
public class JDefinedClassOrInterfaceAdapter
    implements AdapterSchedulable<JSession, JDefinedClassOrInterface> {

  /**
   * Return every {@code JDefinedClassOrInterface} to emit during the given {@code JSession}.
   */
  @Override
  @Nonnull
  public Iterator<JDefinedClassOrInterface> adapt(@Nonnull JSession session)
      throws Exception {
    // Use a copy to scan types in order to support concurrent modification.
    return new ArrayList<JDefinedClassOrInterface>(session.getTypesToEmit()).iterator();
  }
}
