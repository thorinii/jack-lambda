/*
 * Copyright 2008 Google Inc.
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
package com.android.jack.ir.ast;

import com.android.jack.ir.JNodeInternalError;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java local variable definition.
 */
@Description("Java local variable definition")
public class JLocal extends JVariable implements HasEnclosingMethod {

  @CheckForNull
  private JMethodBody enclosingMethodBody;

  public JLocal(SourceInfo info, String name, JType type, int modifier,
      @CheckForNull JMethodBody enclosingMethodBody) {
    super(info, name, type, modifier);
    assert JModifier.isLocalModifier(modifier);
    this.enclosingMethodBody = enclosingMethodBody;
  }

  @Override
  @CheckForNull
  public JMethod getEnclosingMethod() {
    final JAbstractMethodBody enclosingMethodBodyLocal = enclosingMethodBody;

    if (enclosingMethodBodyLocal != null) {
      return enclosingMethodBodyLocal.method;
    }

    return null;
  }

  public void setEnclosingMethodBody(@Nonnull JMethodBody enclosingMethodBody) {
    this.enclosingMethodBody = enclosingMethodBody;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      // Do not visit declStmt, it gets visited within its own code block.
      annotations.traverse(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    annotations.traverse(schedule);
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  @Override
  public void checkValidity() {
    if (!(parent instanceof JMethodBody || parent instanceof JCatchBlock)) {
      throw new JNodeInternalError(this, "Invalid parent");
    }
  }
}
