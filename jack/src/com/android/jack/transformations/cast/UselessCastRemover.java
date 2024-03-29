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

package com.android.jack.transformations.cast;

import com.android.jack.Options;
import com.android.jack.ir.ast.JCastOperation;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JReferenceType;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;
/**
 * Removes useless casts.
 */
@Description("Removes useless casts.")
@Name("UselessCastRemover")
@Constraint(need = JDynamicCastOperation.class)
public class UselessCastRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest request;


    public Visitor(@Nonnull TransformationRequest request) {
      this.request = request;
    }

    @Override
    public void endVisit(@Nonnull JCastOperation cast) {
      JType destType = cast.getCastType();
      JType srcType = cast.getExpr().getType();
      if (srcType instanceof JReferenceType && destType instanceof JReferenceType) {
        if (((JReferenceType) srcType).canBeSafelyUpcast((JReferenceType) destType)) {
          request.append(new Replace(cast, cast.getExpr()));
        }
      }
      super.endVisit(cast);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(UselessCastRemover.class, method)) {
      return;
    }

    TransformationRequest request = new TransformationRequest(method);
    Visitor visitor = new Visitor(request);
    visitor.accept(method);
    request.commit();
  }
}
