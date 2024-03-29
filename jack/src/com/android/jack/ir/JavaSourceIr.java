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

package com.android.jack.ir;

import com.android.jack.backend.dex.annotations.AnnotationMethodDefaultValue;
import com.android.jack.ir.ast.JAbsentArrayDimension;
import com.android.jack.ir.ast.JAddOperation;
import com.android.jack.ir.ast.JAndOperation;
import com.android.jack.ir.ast.JAnnotationLiteral;
import com.android.jack.ir.ast.JArrayLength;
import com.android.jack.ir.ast.JArrayRef;
import com.android.jack.ir.ast.JArrayType;
import com.android.jack.ir.ast.JAsgAddOperation;
import com.android.jack.ir.ast.JAsgBitAndOperation;
import com.android.jack.ir.ast.JAsgBitOrOperation;
import com.android.jack.ir.ast.JAsgBitXorOperation;
import com.android.jack.ir.ast.JAsgConcatOperation;
import com.android.jack.ir.ast.JAsgDivOperation;
import com.android.jack.ir.ast.JAsgModOperation;
import com.android.jack.ir.ast.JAsgMulOperation;
import com.android.jack.ir.ast.JAsgOperation;
import com.android.jack.ir.ast.JAsgShlOperation;
import com.android.jack.ir.ast.JAsgShrOperation;
import com.android.jack.ir.ast.JAsgShruOperation;
import com.android.jack.ir.ast.JAsgSubOperation;
import com.android.jack.ir.ast.JAssertStatement;
import com.android.jack.ir.ast.JBitAndOperation;
import com.android.jack.ir.ast.JBitOrOperation;
import com.android.jack.ir.ast.JBitXorOperation;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JBreakStatement;
import com.android.jack.ir.ast.JByteLiteral;
import com.android.jack.ir.ast.JCaseStatement;
import com.android.jack.ir.ast.JCatchBlock;
import com.android.jack.ir.ast.JCharLiteral;
import com.android.jack.ir.ast.JClassLiteral;
import com.android.jack.ir.ast.JConcatOperation;
import com.android.jack.ir.ast.JConditionalExpression;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JContinueStatement;
import com.android.jack.ir.ast.JDefinedAnnotation;
import com.android.jack.ir.ast.JDefinedClass;
import com.android.jack.ir.ast.JDefinedEnum;
import com.android.jack.ir.ast.JDefinedInterface;
import com.android.jack.ir.ast.JDivOperation;
import com.android.jack.ir.ast.JDoStatement;
import com.android.jack.ir.ast.JDoubleLiteral;
import com.android.jack.ir.ast.JDynamicCastOperation;
import com.android.jack.ir.ast.JEnumField;
import com.android.jack.ir.ast.JEnumLiteral;
import com.android.jack.ir.ast.JEqOperation;
import com.android.jack.ir.ast.JExpressionStatement;
import com.android.jack.ir.ast.JField;
import com.android.jack.ir.ast.JFieldInitializer;
import com.android.jack.ir.ast.JFieldRef;
import com.android.jack.ir.ast.JFloatLiteral;
import com.android.jack.ir.ast.JForStatement;
import com.android.jack.ir.ast.JGtOperation;
import com.android.jack.ir.ast.JGteOperation;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JInstanceOf;
import com.android.jack.ir.ast.JIntLiteral;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JLocal;
import com.android.jack.ir.ast.JLocalRef;
import com.android.jack.ir.ast.JLongLiteral;
import com.android.jack.ir.ast.JLtOperation;
import com.android.jack.ir.ast.JLteOperation;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JMethodBody;
import com.android.jack.ir.ast.JMethodCall;
import com.android.jack.ir.ast.JModOperation;
import com.android.jack.ir.ast.JMulOperation;
import com.android.jack.ir.ast.JNativeMethodBody;
import com.android.jack.ir.ast.JNeqOperation;
import com.android.jack.ir.ast.JNewArray;
import com.android.jack.ir.ast.JNewInstance;
import com.android.jack.ir.ast.JNullLiteral;
import com.android.jack.ir.ast.JNullType;
import com.android.jack.ir.ast.JOrOperation;
import com.android.jack.ir.ast.JParameter;
import com.android.jack.ir.ast.JParameterRef;
import com.android.jack.ir.ast.JPhantomAnnotation;
import com.android.jack.ir.ast.JPhantomClass;
import com.android.jack.ir.ast.JPhantomClassOrInterface;
import com.android.jack.ir.ast.JPhantomEnum;
import com.android.jack.ir.ast.JPhantomInterface;
import com.android.jack.ir.ast.JPostfixDecOperation;
import com.android.jack.ir.ast.JPostfixIncOperation;
import com.android.jack.ir.ast.JPrefixBitNotOperation;
import com.android.jack.ir.ast.JPrefixDecOperation;
import com.android.jack.ir.ast.JPrefixIncOperation;
import com.android.jack.ir.ast.JPrefixNegOperation;
import com.android.jack.ir.ast.JPrefixNotOperation;
import com.android.jack.ir.ast.JPrimitiveType.JBooleanType;
import com.android.jack.ir.ast.JPrimitiveType.JByteType;
import com.android.jack.ir.ast.JPrimitiveType.JCharType;
import com.android.jack.ir.ast.JPrimitiveType.JDoubleType;
import com.android.jack.ir.ast.JPrimitiveType.JFloatType;
import com.android.jack.ir.ast.JPrimitiveType.JIntType;
import com.android.jack.ir.ast.JPrimitiveType.JLongType;
import com.android.jack.ir.ast.JPrimitiveType.JShortType;
import com.android.jack.ir.ast.JPrimitiveType.JVoidType;
import com.android.jack.ir.ast.JReturnStatement;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JShlOperation;
import com.android.jack.ir.ast.JShortLiteral;
import com.android.jack.ir.ast.JShrOperation;
import com.android.jack.ir.ast.JShruOperation;
import com.android.jack.ir.ast.JStringLiteral;
import com.android.jack.ir.ast.JSubOperation;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JSynchronizedBlock;
import com.android.jack.ir.ast.JThisRef;
import com.android.jack.ir.ast.JThrowStatement;
import com.android.jack.ir.ast.JTryStatement;
import com.android.jack.ir.ast.JWhileStatement;
import com.android.jack.ir.ast.marker.GenericSignature;
import com.android.jack.ir.ast.marker.SimpleName;
import com.android.jack.ir.ast.marker.ThisRefTypeInfo;
import com.android.jack.ir.ast.marker.ThrownExceptionMarker;
import com.android.jack.transformations.EmptyClinit;
import com.android.jack.transformations.ast.BooleanTestOutsideIf;
import com.android.jack.transformations.ast.ImplicitBoxingAndUnboxing;
import com.android.jack.transformations.ast.ImplicitCast;
import com.android.jack.transformations.ast.InitInNewArray;
import com.android.jack.transformations.ast.JPrimitiveClassLiteral;
import com.android.jack.transformations.ast.MultiDimensionNewArray;
import com.android.jack.transformations.ast.removeinit.FieldInitMethod;
import com.android.jack.transformations.ast.removeinit.FieldInitMethodCall;
import com.android.jack.transformations.ast.switches.UselessSwitches;
import com.android.sched.item.AbstractComponent;
import com.android.sched.item.ComposedOf;
import com.android.sched.item.Description;
import com.android.sched.item.Name;

/**
 * Tag containing all JNodes, tags or markers that represent the Java source.
 */
@Name("Java Source IR")
@Description("All JNodes, tags or markers that represent the Java source.")
@ComposedOf({AnnotationMethodDefaultValue.class,
    BooleanTestOutsideIf.class,
    EmptyClinit.class,
    FieldInitMethod.class,
    FieldInitMethodCall.class,
    ImplicitBoxingAndUnboxing.class,
    ImplicitCast.class,
    InitInNewArray.class,
    JAbsentArrayDimension.class,
    JAddOperation.class,
    JAndOperation.class,
    JAnnotationLiteral.class,
    JArrayLength.class,
    JArrayRef.class,
    JArrayType.class,
    JArrayType.class,
    JAsgAddOperation.class,
    JAsgBitAndOperation.class,
    JAsgBitOrOperation.class,
    JAsgBitXorOperation.class,
    JAsgConcatOperation.class,
    JAsgDivOperation.class,
    JAsgModOperation.class,
    JAsgMulOperation.class,
    JAsgOperation.class,
    JAsgShlOperation.class,
    JAsgShrOperation.class,
    JAsgShruOperation.class,
    JAsgSubOperation.class,
    JAssertStatement.class,
    JBitAndOperation.class,
    JBitOrOperation.class,
    JBitXorOperation.class,
    JBlock.class,
    JBooleanLiteral.class,
    JBooleanType.class,
    JBreakStatement.class,
    JByteLiteral.class,
    JByteType.class,
    JCaseStatement.class,
    JCatchBlock.class,
    JCharLiteral.class,
    JCharType.class,
    JClassLiteral.class,
    JConcatOperation.class,
    JConditionalExpression.class,
    JConstructor.class,
    JContinueStatement.class,
    JDefinedAnnotation.class,
    JDefinedClass.class,
    JDefinedEnum.class,
    JDefinedInterface.class,
    JDivOperation.class,
    JDoStatement.class,
    JDoubleLiteral.class,
    JDoubleType.class,
    JDynamicCastOperation.class,
    JEnumField.class,
    JEnumLiteral.class,
    JEqOperation.class,
    JExpressionStatement.class,
    JField.class,
    JFieldInitializer.class,
    JFieldRef.class,
    JFloatLiteral.class,
    JFloatType.class,
    JForStatement.class,
    JGteOperation.class,
    JGtOperation.class,
    JIfStatement.class,
    JInstanceOf.class,
    JIntLiteral.class,
    JIntType.class,
    JLabel.class,
    JLabeledStatement.class,
    JLocal.class,
    JLocalRef.class,
    JLongLiteral.class,
    JLongType.class,
    JLteOperation.class,
    JLtOperation.class,
    JMethod.class,
    JMethodBody.class,
    JMethodCall.class,
    JModOperation.class,
    JMulOperation.class,
    JNativeMethodBody.class,
    JNeqOperation.class,
    JNewArray.class,
    JNewInstance.class,
    JNullLiteral.class,
    JNullType.class,
    JOrOperation.class,
    JParameter.class,
    JParameterRef.class,
    JPhantomAnnotation.class,
    JPhantomClass.class,
    JPhantomClassOrInterface.class,
    JPhantomEnum.class,
    JPhantomInterface.class,
    JPostfixDecOperation.class,
    JPostfixIncOperation.class,
    JPrefixBitNotOperation.class,
    JPrefixDecOperation.class,
    JPrefixIncOperation.class,
    JPrefixNegOperation.class,
    JPrefixNotOperation.class,
    JPrimitiveClassLiteral.class,
    JSession.class,
    JReturnStatement.class,
    JShlOperation.class,
    JShortLiteral.class,
    JShortType.class,
    JShrOperation.class,
    JShruOperation.class,
    JStringLiteral.class,
    JSubOperation.class,
    JSwitchStatement.class,
    JSwitchStatement.SwitchWithEnum.class,
    JSynchronizedBlock.class,
    JThisRef.class,
    JThrowStatement.class,
    JTryStatement.class,
    JTryStatement.FinallyBlock.class,
    JTryStatement.TryWithResourcesForm.class,
    JVoidType.class,
    JWhileStatement.class,
    MultiDimensionNewArray.class,
    GenericSignature.class,
    SimpleName.class,
    ThisRefTypeInfo.class,
    ThrownExceptionMarker.class,
    UselessSwitches.class})
public class JavaSourceIr implements AbstractComponent {
}
