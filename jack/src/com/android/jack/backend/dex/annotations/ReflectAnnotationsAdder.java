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

package com.android.jack.backend.dex.annotations;

import com.android.jack.backend.dex.DexAnnotations;
import com.android.jack.backend.dex.annotations.tag.ReflectAnnotations;
import com.android.jack.dx.rop.code.AccessFlags;
import com.android.jack.ir.ast.Annotable;
import com.android.jack.ir.ast.JAnnotation;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JArrayLiteral;
import com.android.jack.ir.ast.JClass;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JClassOrInterface;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLiteral;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodId;
import com.android.jack.ir.ast.JMethodLiteral;
import com.android.jack.ir.ast.JModifier;
import com.android.jack.ir.ast.JNameValuePair;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JRetentionPolicy;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JType;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.ast.MethodKind;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JPhantomLookup;
import com.android.jack.shrob.obfuscation.FinalNames;
import com.android.jack.transformations.request.AddAnnotation;
import com.android.jack.transformations.request.PutNameValuePair;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.sched.item.Description;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Protect;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;
import com.android.sched.schedulable.With;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * Add annotations used by reflection.
 */
@Description("Add annotations used by reflection")
@Synchronized
@Transform(add = {ReflectAnnotations.class, JAnnotationLiteral.class, JNameValuePair.class,
    JClassLiteral.class, JStringLiteral.class, JMethodLiteral.class, JArrayLiteral.class,
    JNullLiteral.class, JIntLiteral.class, ClassAnnotationSchedulingSeparator.SeparatorTag.class})
@Constraint(need = {GenericSignature.class, SimpleName.class, FinalNames.class})
@Protect(add = {GenericSignature.class, SimpleName.class},
    unprotect = @With(remove = ReflectAnnotations.class))
public class ReflectAnnotationsAdder implements RunnableSchedulable<JDefinedClassOrInterface> {

  private static class Visitor extends JVisitor {

    @Nonnull
    private final TransformationRequest request;
    @Nonnull
    private final JClass javaLangClass;

    @Nonnull
    private static final String ELT_VALUE = "value";
    @Nonnull
    private static final String ELT_NAME = "name";
    @Nonnull
    private static final String ELT_ACCESS_FLAGS = "accessFlags";
    @Nonnull
    private final JAnnotation defaultAnnotation;
    @Nonnull
    private final JAnnotation signatureAnnotation;
    @Nonnull
    private final JAnnotation enclosingMethodAnnotation;
    @Nonnull
    private final JAnnotation enclosingClassAnnotation;
    @Nonnull
    private final JAnnotation throwsAnnotation;
    @Nonnull
    private final JAnnotation innerAnnotation;
    @Nonnull
    private final JAnnotation memberClassAnnotation;

    public Visitor(@Nonnull TransformationRequest request, @Nonnull JPhantomLookup lookup) {
      this.request = request;
      javaLangClass = lookup.getClass(CommonTypes.JAVA_LANG_CLASS);
      defaultAnnotation = lookup.getAnnotation(DexAnnotations.ANNOTATION_ANNOTATION_DEFAULT);
      signatureAnnotation = lookup.getAnnotation(DexAnnotations.ANNOTATION_SIGNATURE);
      enclosingMethodAnnotation = lookup.getAnnotation(DexAnnotations.ANNOTATION_ENCLOSING_METHOD);
      enclosingClassAnnotation = lookup.getAnnotation(DexAnnotations.ANNOTATION_ENCLOSING_CLASS);
      throwsAnnotation = lookup.getAnnotation(DexAnnotations.ANNOTATION_THROWS);
      innerAnnotation = lookup.getAnnotation(DexAnnotations.ANNOTATION_INNER);
      memberClassAnnotation = lookup.getAnnotation(DexAnnotations.ANNOTATION_MEMBER_CLASSES);
    }

    @Nonnull
    private JMethodId getOrCreateMethodId(@Nonnull JAnnotation type, @Nonnull String name) {
      return type.getOrCreateMethodId(
          name, Collections.<JType>emptyList(), MethodKind.INSTANCE_VIRTUAL);
    }

    @Override
    public boolean visit(@Nonnull JMethod x) {
      return false;
    }

    @Override
    public void endVisit(@Nonnull JDefinedClassOrInterface x) {
      JClassOrInterface enclosingType = x.getEnclosingType();
      if (enclosingType != null) {
        addInnerClass(x);
        boolean isLocal =
            (x instanceof JDefinedClass) && ((JDefinedClass) x).getEnclosingMethod() != null;
        // !anonymous && !local
        if (!isLocal && !JModifier.isAnonymousType(x.getModifier())) {
          addMemberClasses(x);
        }
        if (isLocal) {
          addEnclosingMethod(x);
        } else {
          addEnclosingClass(x);
        }
      }
      GenericSignature marker = x.getMarker(GenericSignature.class);
      if (marker != null) {
        addSignature(x, marker.getGenericSignature(), x.getSourceInfo());
      }
    }

    @Override
    public void endVisit(@Nonnull JField x) {
      GenericSignature marker = x.getMarker(GenericSignature.class);
      if (marker != null) {
        addSignature(x, marker.getGenericSignature(), x.getSourceInfo());
      }
    }

    @Override
    public void endVisit(@Nonnull JMethod x) {
      addThrows(x);
      GenericSignature marker = x.getMarker(GenericSignature.class);
      if (marker != null) {
        String genericSignature = marker.getGenericSignature();
        if (genericSignature != null) {
          addSignature(x, genericSignature, x.getSourceInfo());
        }
      }
    }

    private void addSignature(@Nonnull Annotable annotable, @Nonnull String signature,
        @Nonnull SourceInfo info) {
      JAnnotationLiteral annotation =
          createAnnotation(annotable, signatureAnnotation, info);
      JArrayLiteral literal = buildSignatureAnnotationValue(signature, info);
      JMethodId methodId = getOrCreateMethodId(signatureAnnotation, ELT_VALUE);
      JNameValuePair valuePair = new JNameValuePair(info, methodId, literal);
      assert annotation.getNameValuePair(methodId) == null
          : "Type can not have more than one generic signature";
      request.append(new PutNameValuePair(annotation, valuePair));
    }

    private void addEnclosingMethod(@Nonnull JDefinedClassOrInterface type) {
      if (type instanceof JDefinedClass) {
        JDefinedClass classType = (JDefinedClass) type;
        JMethod method = classType.getEnclosingMethod();
        if (method != null) {
          SourceInfo info = type.getSourceInfo();
          JAnnotationLiteral annotation = createAnnotation(type, enclosingMethodAnnotation, info);
          JMethodLiteral newLiteral = new JMethodLiteral(method, info);
          JMethodId methodId = getOrCreateMethodId(enclosingMethodAnnotation, ELT_VALUE);
          JNameValuePair valuePair = new JNameValuePair(info, methodId, newLiteral);
          assert annotation.getNameValuePair(methodId) == null
              : "Type can not have more than one enclosing method";
          request.append(new PutNameValuePair(annotation, valuePair));
        }
      }
    }

    private void addThrows(@Nonnull JMethod method) {
      ThrownExceptionMarker marker = method.getMarker(ThrownExceptionMarker.class);
      if (marker != null) {
        List<JClass> throwns = marker.getThrownExceptions();
        SourceInfo info = method.getSourceInfo();
        JAnnotationLiteral annotation = createAnnotation(method, throwsAnnotation, info);
        List<JLiteral> classLiterals = new ArrayList<JLiteral>();
        for (JClass thrown : throwns) {
          classLiterals.add(new JClassLiteral(info, thrown, javaLangClass));
        }
        JMethodId methodId = getOrCreateMethodId(throwsAnnotation, ELT_VALUE);
        addClassLiterals(classLiterals, annotation, methodId, info);
      }
   }

    private void addMemberClasses(@Nonnull JDefinedClassOrInterface innerType) {
      JClassOrInterface enclosingType = innerType.getEnclosingType();
      if (!enclosingType.isExternal() && enclosingType instanceof JDefinedClassOrInterface) {
          SourceInfo info = enclosingType.getSourceInfo();
          JAnnotationLiteral annotation =
              getAnnotation((JDefinedClassOrInterface) enclosingType, memberClassAnnotation, info);
          JLiteral newValue = new JClassLiteral(info, innerType, javaLangClass);
          List<JLiteral> literals = new ArrayList<JLiteral>();
          literals.add(newValue);
          JMethodId methodId = getOrCreateMethodId(memberClassAnnotation, ELT_VALUE);
          addClassLiterals(literals, annotation, methodId, info);
      }
    }

    private void addEnclosingClass(@Nonnull JDefinedClassOrInterface innerType) {
      SourceInfo info = innerType.getSourceInfo();
      JAnnotationLiteral annotation = createAnnotation(innerType, enclosingClassAnnotation, info);
      JLiteral newValue = new JClassLiteral(info, innerType.getEnclosingType(), javaLangClass);
      List<JLiteral> literals = new ArrayList<JLiteral>();
      literals.add(newValue);
      JMethodId methodId = getOrCreateMethodId(enclosingClassAnnotation, ELT_VALUE);
      JNameValuePair valuePair = new JNameValuePair(info, methodId, newValue);
      request.append(new PutNameValuePair(annotation, valuePair));
    }

    private void addInnerClass(@Nonnull JDefinedClassOrInterface innerType) {
      SourceInfo info = innerType.getSourceInfo();
      JAnnotationLiteral annotation = createAnnotation(innerType, innerAnnotation, info);
      SimpleName marker = innerType.getMarker(SimpleName.class);
      assert marker != null;
      String innerShortName = marker.getSimpleName();
      JLiteral newValue;
      if (!innerShortName.isEmpty()) {
        newValue = new JStringLiteral(info, innerShortName);
      } else {
        newValue = new JNullLiteral(info);
      }
      JMethodId nameMethodId = getOrCreateMethodId(innerAnnotation, ELT_NAME);
      JNameValuePair nameValuePair = new JNameValuePair(info, nameMethodId, newValue);
      request.append(new PutNameValuePair(annotation, nameValuePair));
      int accessFlags = innerType.getModifier();

      // An anonymous class should not be flagged as final
      if (JModifier.isAnonymousType(accessFlags)) {
        accessFlags &= ~JModifier.FINAL;
      }

      // Add static flag on inner interfaces
      if (JModifier.isInterface(accessFlags)) {
        accessFlags |= JModifier.STATIC;
      }

      accessFlags &= AccessFlags.INNER_CLASS_FLAGS;

      JMethodId flagsMethodId = getOrCreateMethodId(innerAnnotation, ELT_ACCESS_FLAGS);
      JNameValuePair flagsValuePair =
          new JNameValuePair(info, flagsMethodId, new JIntLiteral(info, accessFlags));
      request.append(new PutNameValuePair(annotation, flagsValuePair));
    }

    /**
     * Adds class literals in {@code innerAnnotation} as values of {@code element}
     */
    private void addClassLiterals(@Nonnull List<JLiteral> literals,
        @Nonnull JAnnotationLiteral annotation, @Nonnull JMethodId methodId,
        @Nonnull SourceInfo info) {
      JNameValuePair valuePair = annotation.getNameValuePair(methodId);
      if (valuePair != null) {
        JLiteral oldValue = valuePair.getValue();
        if (oldValue instanceof JArrayLiteral) {
          literals.addAll(((JArrayLiteral) oldValue).getValues());
        } else {
          assert oldValue instanceof JClassLiteral;
          literals.add(oldValue);
        }
      }
      JArrayLiteral array = new JArrayLiteral(info, literals);
      valuePair = new JNameValuePair(info, methodId, array);
      request.append(new PutNameValuePair(annotation, valuePair));
    }

    @Nonnull
    private JAnnotationLiteral createAnnotation(@Nonnull Annotable annotable,
        @Nonnull JAnnotation annotationType,  @Nonnull SourceInfo info) {
      JAnnotationLiteral annotation =
          new JAnnotationLiteral(info, JRetentionPolicy.SYSTEM, annotationType);
      request.append(new AddAnnotation(annotation, annotable));
      return annotation;
    }

    private boolean isSystemAnnotation(@Nonnull JAnnotation annotationType) {
      if (annotationType.isSameType(defaultAnnotation)
          || annotationType.isSameType(enclosingClassAnnotation)
          || annotationType.isSameType(enclosingMethodAnnotation)
          || annotationType.isSameType(innerAnnotation)
          || annotationType.isSameType(memberClassAnnotation)
          || annotationType.isSameType(signatureAnnotation)
          || annotationType.isSameType(throwsAnnotation)) {
        return true;
      }
      return false;
    }

    @Nonnull
    private JAnnotationLiteral getAnnotation(@Nonnull Annotable annotable,
        @Nonnull JAnnotation annotationType,  @Nonnull SourceInfo info) {
      assert isSystemAnnotation(annotationType);
      JAnnotationLiteral annotation = null;
      Collection<JAnnotationLiteral> annotations = annotable.getAnnotations(annotationType);
      if (annotations.isEmpty()) {
        annotation = createAnnotation(annotable, annotationType, info);
      } else {
        assert annotations.size() == 1;
        annotation = annotations.iterator().next();
      }
      return annotation;
    }

    @Nonnull
    private JArrayLiteral buildSignatureAnnotationValue (@Nonnull String signature,
        @Nonnull SourceInfo info) {
      int sigLength = signature.length();
      List<JLiteral> pieces = new ArrayList<JLiteral>();
      for (int at = 0; at < sigLength; /*at*/) {
        char c = signature.charAt(at);
        int endAt = at + 1;
        if (c == 'L') {
            // Scan to ';' or '<'. Consume ';' but not '<'.
            while (endAt < sigLength) {
                c = signature.charAt(endAt);
                if (c == ';') {
                    endAt++;
                    break;
                } else if (c == '<') {
                    break;
                }
                endAt++;
            }
        } else {
            // Scan to 'L' without consuming it.
            while (endAt < sigLength) {
                c = signature.charAt(endAt);
                if (c == 'L') {
                    break;
                }
                endAt++;
            }
        }
        pieces.add(new JStringLiteral(info, signature.substring(at, endAt)));
        at = endAt;
      }
      return new JArrayLiteral(info, pieces);
    }
  }

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface declaredType) throws Exception {
    TransformationRequest tr = new TransformationRequest(declaredType);
    Visitor visitor = new Visitor(tr, declaredType.getSession().getPhantomLookup());
    visitor.accept(declaredType);
    tr.commit();
  }
}
