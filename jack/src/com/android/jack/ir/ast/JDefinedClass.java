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

import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.load.ClassOrInterfaceLoader;
import com.android.sched.item.Component;
import com.android.sched.item.Description;
import com.android.sched.scheduler.ScheduleInstance;
import com.android.sched.transform.TransformRequest;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Java class type reference expression.
 */
@Description("Java class type reference expression")
public class JDefinedClass extends JDefinedClassOrInterface implements CanBeSetFinal, JClass {

  @CheckForNull
  private JClass superClass;

  @CheckForNull
  private JMethod enclosingMethod;

  public JDefinedClass(@Nonnull SourceInfo info, @Nonnull String name, int modifier,
      @Nonnull JPackage enclosingPackage, @Nonnull ClassOrInterfaceLoader loader) {
    super(info, name, modifier, enclosingPackage, loader);
  }

  @CheckForNull
  @Override
  public final JClass getSuperClass() {
    loader.ensureHierarchy(this);
    return superClass;
  }

  /**
   * Sets this type's super class.
   */
  public final void setSuperClass(@CheckForNull JClass superClass) {
    this.superClass = superClass;
  }

  public void setEnclosingMethod(@CheckForNull JMethod enclosingMethod) {
    this.enclosingMethod = enclosingMethod;
  }

  @CheckForNull
  public JMethod getEnclosingMethod() {
    loader.ensureEnclosing(this);
    return enclosingMethod;
  }

  @Override
  public void traverse(@Nonnull JVisitor visitor) {
    if (visitor.visit(this)) {
      if (visitor.needLoading()) {
        loader.ensureFields(this);
        loader.ensureMethods(this);
        loader.ensureAnnotations(this);
      }
      visitor.accept(fields);
      visitor.accept(methods);
      annotations.traverse(visitor);
    }
    visitor.endVisit(this);
  }

  @Override
  public void traverse(@Nonnull ScheduleInstance<? super Component> schedule) throws Exception {
    schedule.process(this);
    for (JField field : fields) {
      field.traverse(schedule);
    }
    for (JMethod method : methods) {
      method.traverse(schedule);
    }
    annotations.traverse(schedule);
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!transform(fields, existingNode, (JField) newNode, transformation)) {
      if (!transform(methods, existingNode, (JMethod) newNode, transformation)) {
        super.transform(existingNode, newNode, transformation);
      }
    }
  }

  @Override
  public void visit(@Nonnull JVisitor visitor, @Nonnull TransformRequest transformRequest)
      throws Exception {
    visitor.visit(this, transformRequest);
  }

  public boolean isSubTypeOf(@Nonnull JReferenceType jclass) {
    if (jclass instanceof JClass) {
      JClass currentClass = this;
      do {
        currentClass = ((JDefinedClass) currentClass).getSuperClass();

        if (currentClass != null && jclass.isSameType(currentClass)) {
          return true;
        }
      } while (currentClass instanceof JDefinedClass);
    }

    return false;
  }

  private boolean canBeSafelyUpcast(@Nonnull JInterface castTo) {
    JClass currentClass = this;
    do {
      JDefinedClass currentDefinedClass = (JDefinedClass) currentClass;
      if (currentDefinedClass.implementsInterface(castTo)) {
        return true;
      }
      currentClass = currentDefinedClass.getSuperClass();
    } while (currentClass instanceof JDefinedClass);

    return false;
  }

  @Override
  public boolean canBeSafelyUpcast(@Nonnull JReferenceType castTo) {
    if (isTrivialCast(castTo)) {
      return true;
    }
    if (castTo instanceof JInterface) {
      return canBeSafelyUpcast((JInterface) castTo);
    } else {
      return isSubTypeOf(castTo);
    }
  }
}
