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

package com.android.jack.jayce.v0002.util;

import com.android.jack.ir.ast.JMethodCall.DispatchKind;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * A helper class to encode {@link DispatchKind} enum values in Jayce format.
 */
public class DispatchKindIdHelper {

  @Nonnull
  private static DispatchKind[] values;

  @Nonnull
  private static byte[] ids;

  static {
    values = new DispatchKind[2];
    values[0] = DispatchKind.VIRTUAL;
    values[1] = DispatchKind.DIRECT;

    ids = new byte[2];
    ids[DispatchKind.VIRTUAL.ordinal()]  = 0;
    ids[DispatchKind.DIRECT.ordinal()]   = 1;
  }

  @Nonnegative
  public static byte getId(@Nonnull Enum<?> enumValue) {
    return ids[enumValue.ordinal()];
  }

  @Nonnull
  public static Enum<?> getValue(@Nonnegative byte id) {
    return values[id];
  }
}
