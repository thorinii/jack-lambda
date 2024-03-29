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

package com.android.jack.util;

import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JVisitorWithConcurrentModification;

import javax.annotation.Nonnull;

/**
 * A {@link JVisitorWithConcurrentModification} that does not visit annotations values.
 */
// TODO(yroussel) a better way to filter out ?
public class AnnotationSkipperVisitor extends JVisitorWithConcurrentModification {
  public AnnotationSkipperVisitor() {
  }

  @Override
  public boolean visit(@Nonnull JAnnotationLiteral stmt) {
    return false;
  }

  @Override
  public void endVisit(@Nonnull JAnnotationLiteral annotationLiteral) {
  }

}
