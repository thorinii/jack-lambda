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


import com.android.jack.ir.StringInterner;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;

import java.util.Collection;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Base class for any storage location.
 */
@Description("Any storage location")
public abstract class JVariable extends JNode implements HasName, CanBeSetFinal,
    CanBeRenamed, HasType, Annotable {

  @CheckForNull
  private String name;
  @Nonnull
  private final JType type;
  protected final AnnotationSet annotations = new AnnotationSet();

  protected int modifier;

  JVariable(SourceInfo info, @CheckForNull String name, @Nonnull JType type, int modifier) {
    super(info);
    assert (type != null);
    assert (type != JPrimitiveTypeEnum.VOID.getType());
    this.name = name == null ? null : StringInterner.get().intern(name);
    this.type = type;
    this.modifier = modifier;
  }

  JVariable(SourceInfo info, @Nonnull JType type, int modifier) {
    this(info, null, type, modifier);
  }

  /**
   * @return the modifier
   */
  public int getModifier() {
    return modifier;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public void setName(@CheckForNull String name) {
    this.name = name == null ? null : StringInterner.get().intern(name);
  }

  @Override
  @Nonnull
  public JType getType() {
    return type;
  }

  @Override
  public boolean isFinal() {
    return (JModifier.isFinal(modifier));
  }

  public boolean isSynthetic() {
    return (JModifier.isSynthetic(modifier));
  }

  @Override
  public void setFinal() {
    modifier |= JModifier.FINAL;
  }

  public void setSynthetic() {
    modifier |= JModifier.SYNTHETIC;
  }

  @Override
  public void addAnnotation(@Nonnull JAnnotationLiteral annotation) {
    annotations.addAnnotation(annotation);
  }

  @Override
  @Nonnull
  public List<JAnnotationLiteral> getAnnotations(@Nonnull JAnnotation annotationType) {
    return annotations.getAnnotation(annotationType);
  }

  @Override
  @Nonnull
  public Collection<JAnnotationLiteral> getAnnotations() {
    return annotations.getAnnotations();
  }

  @Override
  @Nonnull
  public Collection<JAnnotation> getAnnotationTypes() {
    return annotations.getAnnotationTypes();
  }

  @Override
  protected void transform(@Nonnull JNode existingNode, @CheckForNull JNode newNode,
      @Nonnull Transformation transformation) throws UnsupportedOperationException {
    if (!annotations.transform(existingNode, newNode, transformation)) {
      super.transform(existingNode, newNode, transformation);
    }
  }
}
