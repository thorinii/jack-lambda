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

package com.android.jack.jayce;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPackage;
import com.android.jack.ir.ast.JTypeLookupException;
import com.android.jack.lookup.JMethodLookupException;

import javax.annotation.Nonnull;

/**
 * Common interface for {@link Node} representing a {@link JDefinedClassOrInterface}.
 */
public interface DeclaredTypeNode extends Node {

  void updateToStructure(@Nonnull JDefinedClassOrInterface loaded,
      @Nonnull JayceClassOrInterfaceLoader classOrInterfaceLoader) throws JTypeLookupException,
      JMethodLookupException;

  @Nonnull
  JDefinedClassOrInterface create(
      @Nonnull JPackage loading, @Nonnull JayceClassOrInterfaceLoader classOrInterfaceLoader);

  @Nonnull
  String getSignature();

  @Nonnull
  MethodNode getMethodNode(@Nonnull JMethod loaded);

  @Nonnull
  NodeLevel getLevel();

}
