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

package com.android.jack.transformations.uselessif;

import com.android.jack.Options;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.transformations.SanityChecks;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;

import javax.annotation.Nonnull;

/**
 * This visitor checks that there is no if statement with a condition that is a boolean literal.
 */
@Description("Checks that there is no if statement with a condition that is a boolean literal.")
@Name("UselessIfChecker")
@Constraint(need = {JIfStatement.class})
@Support(SanityChecks.class)
public class UselessIfChecker implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  private static class UselessIfCheckerVisitor extends JVisitor {

    @Override
    public boolean visit(@Nonnull JIfStatement ifStmt) {

      if (ifStmt.getIfExpr() instanceof JBooleanLiteral) {
        throw new AssertionError("All useless if should have been removed");
      }

      return super.visit(ifStmt);
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    UselessIfCheckerVisitor visitor = new UselessIfCheckerVisitor();
    visitor.accept(method);
  }

}
