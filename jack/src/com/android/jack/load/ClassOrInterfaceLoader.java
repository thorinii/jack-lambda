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

import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.sched.marker.Marker;
import com.android.sched.util.location.Location;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * Loader for class or interface.
 */
public interface ClassOrInterfaceLoader {

  public void ensureHierarchy(@Nonnull JDefinedClassOrInterface loaded);

  public void ensureMarkers(@Nonnull JDefinedClassOrInterface loaded);

  public void ensureMarker(@Nonnull JDefinedClassOrInterface loaded,
      @Nonnull Class<? extends Marker> cls);

  public void ensureEnclosing(@Nonnull JDefinedClassOrInterface loaded);

  public void ensureInners(@Nonnull JDefinedClassOrInterface loaded);

  public void ensureAnnotations(@Nonnull JDefinedClassOrInterface loaded);

  public void ensureAnnotation(@Nonnull JDefinedClassOrInterface loaded,
      @Nonnull JAnnotation annotation);

  public void ensureMethods(@Nonnull JDefinedClassOrInterface loaded);

  /**
   * Attempt to load defined method, do nothing if there is no such method to load.
   */
  public void ensureMethod(@Nonnull JDefinedClassOrInterface loaded,
      @Nonnull String name, @Nonnull List<? extends JType> args, @Nonnull JType returnType);

  public void ensureFields(@Nonnull JDefinedClassOrInterface loaded);

  /**
   * Attempt to load fields with the given name, do nothing if there is no corresponding field to
   * load.
   */
  public void ensureFields(@Nonnull JDefinedClassOrInterface loaded, @Nonnull String fieldName);

  public void ensureModifier(@Nonnull JDefinedClassOrInterface loaded);

  public void ensureRetentionPolicy(@Nonnull JDefinedAnnotation loaded);

  @Nonnull
  public Location getLocation(@Nonnull JDefinedClassOrInterface loaded);
}
