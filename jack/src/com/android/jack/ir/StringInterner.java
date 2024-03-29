/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.android.jack.ir;

import com.google.common.collect.Interner;
import com.google.common.collect.Interners;

import javax.annotation.Nonnull;

/**
 * A utility class for reducing String memory waste. Note that this does not use
 * the String.intern() method which would prevent GC and fill the PermGen space.
 * Instead, we use a Google Collections WeakInterner.
 */
public class StringInterner {
  private static final StringInterner instance = new StringInterner();

  public static StringInterner get() {
    return instance;
  }

  @Nonnull
  private final Interner<String> stringPool = Interners.newWeakInterner();

  protected StringInterner() {
  }

  @Nonnull
  public String intern(@Nonnull String s) {
    return stringPool.intern(s);
  }

}
