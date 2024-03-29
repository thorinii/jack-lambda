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

package com.android.jack.transformations.ast.string;

import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.ThisRefTypeInfo;
import com.android.jack.signature.GenericSignatureParser;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Use;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Split type generic signature of {@code OriginalTypeInfo} and {@code ThisRefTypeInfo} into more
 * specific string literals.
 */
@Description("Split type generic signature into more specific string literals.")
@Name("TypeGenericSignatureSplitter")
@Constraint(need = GenericSignature.class)
@Use(GenericSignatureRefiner.class)
public class TypeGenericSignatureSplitter implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public void run(@Nonnull JDefinedClassOrInterface declaredType) throws Exception {
    if (declaredType.isExternal()) {
      return;
    }

    GenericSignature marker = declaredType.getMarker(GenericSignature.class);
    if (marker != null) {
      CharSequence newSignature = getSplittedSignature(marker.getGenericSignature());
      if (newSignature != null) {
        marker.setGenericSignature(newSignature);
      }
    }

    ThisRefTypeInfo thisMarker = declaredType.getMarker(ThisRefTypeInfo.class);
    if (thisMarker != null) {
      CharSequence newSignature = getSplittedSignature(thisMarker.getGenericSignature());
      if (newSignature != null) {
        thisMarker.setGenericSignature(newSignature);
      }
    }
  }

  @CheckForNull
  private CharSequence getSplittedSignature(@CheckForNull String oldSignature) {
    if (oldSignature == null || oldSignature.isEmpty()) {
      return null;
    }

    GenericSignatureRefiner parserActions = new GenericSignatureRefiner();
    GenericSignatureParser<JType> parser = new GenericSignatureParser<JType>(parserActions);
    parser.parseClassSignature(oldSignature);
    assert parserActions.getNewSignature().toString().equals(oldSignature);
    return parserActions.getNewSignature();
  }
}
