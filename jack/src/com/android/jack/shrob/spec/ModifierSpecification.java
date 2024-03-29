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

package com.android.jack.shrob.spec;

import com.android.jack.ir.ast.JModifier;

import javax.annotation.Nonnull;

/**
 * Class representing a modifier in a {@code field}, {@code method} or {@code class specification}.
 */
public class ModifierSpecification implements Specification<Integer> {
  private static final int ACCESSIBILITY_FLAGS =
      JModifier.PUBLIC
      | JModifier.PROTECTED
      | JModifier.PRIVATE;

  private int modifier;

  private int modifierWithNegator;

  public void addModifier(int modifier, boolean hasNegator) {
    if (hasNegator) {
      this.modifierWithNegator |= modifier;
    } else {
      this.modifier |= modifier;
    }
  }

  @Override
  public boolean matches(@Nonnull Integer t) {
    // Combining multiple flags is allowed (e.g. public static).
    // It means that both access flags have to be set (e.g. public and static),
    // except when they are conflicting, in which case at least one of them has
    // to be set (e.g. at least public or protected).
    int toCompare = t.intValue();
    int accessflags = toCompare & ACCESSIBILITY_FLAGS;
    int accessflagsSpec = modifier & ACCESSIBILITY_FLAGS;
    if (accessflagsSpec != 0) {
      if ((accessflags | accessflagsSpec) != accessflagsSpec) {
        return false;
      }
      // If the visibility is "package" but the specification isn't,
      // the modifier doesn't match
      if (accessflags == 0) {
        return false;
      }
    }

    int negatorAccessFlags = modifierWithNegator & ACCESSIBILITY_FLAGS;
    if (negatorAccessFlags != 0) {
      if ((accessflags & negatorAccessFlags) != 0) {
        return false;
      }
    }

    int otherflags = toCompare & ~ACCESSIBILITY_FLAGS;
    int otherflagsSpec = modifier & ~ACCESSIBILITY_FLAGS;
    if ((otherflags & otherflagsSpec) != otherflagsSpec) {
      return false;
    }

    int otherflagsSpecWithNegator = modifierWithNegator & ~ACCESSIBILITY_FLAGS;
    if (otherflagsSpecWithNegator != 0) {
      return ((otherflagsSpecWithNegator & ~otherflags) == otherflagsSpecWithNegator);
    }
    return true;
  }

  @Override
  @Nonnull
  public String toString() {
    StringBuilder sb = new StringBuilder();
    if (modifier != 0) {
      sb.append("0x");
      sb.append(Integer.toHexString(modifier));
      sb.append(' ');
    }

    if (modifierWithNegator != 0) {
      sb.append("!");
      sb.append("0x");
      sb.append(Integer.toHexString(modifierWithNegator));
    }

    return sb.toString();
  }
}