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

package com.android.jack.transformations;

import com.android.jack.Options;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.util.config.ThreadConfig;

import java.util.List;

import javax.annotation.Nonnull;
/**
 * This {@link RunnableSchedulable} remove empty clinit methods.
 */
@Description("Remove empty clinit methods")
@Constraint(no = {InitializationExpression.class})
@Transform(remove = {EmptyClinit.class})
public class EmptyClinitRemover implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (JMethod.isClinit(method) && filter.accept(this.getClass(), method)) {
      JMethodBody body = (JMethodBody) method.getBody();
      assert body != null;
      List<JStatement> stmts = body.getStatements();
      if (stmts.isEmpty() || stmts.get(0) instanceof JReturnStatement) {
        TransformationRequest tr = new TransformationRequest(method.getEnclosingType());
        tr.append(new Remove(method));
        tr.commit();
      }
    }
  }
}
