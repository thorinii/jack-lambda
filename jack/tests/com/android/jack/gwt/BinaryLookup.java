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

package com.android.jack.gwt;

import com.android.jack.Jack;
import com.android.jack.Main;
import com.android.jack.Options;
import com.android.jack.TestTools;
import com.android.jack.ir.ast.JConstructor;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JPrimitiveType.JPrimitiveTypeEnum;
import com.android.jack.ir.ast.JSession;
import com.android.jack.ir.ast.JType;
import com.android.jack.lookup.CommonTypes;
import com.android.jack.lookup.JLookup;
import com.android.sched.util.RunnableHooks;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class BinaryLookup {

  private static RunnableHooks hooks;
  private static JLookup lookup;
  private static JSession session;

  @BeforeClass
  public static void setUpClass() throws Exception {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);

    Options fiboArgs = TestTools.buildCommandLineArgs(
        TestTools.getJackTestFromBinaryName("com/android/jack/fibonacci/jack/Fibo"));
    fiboArgs.addProperty(Options.METHOD_FILTER.getName(), "reject-all-methods");
    hooks = new RunnableHooks();
    session = TestTools.buildSession(fiboArgs, hooks);
    lookup = session.getLookup();
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    hooks.runHooks();
  }

  @Test
  public void lookupJavaLangString() {
    JType jls = lookup.getType(CommonTypes.JAVA_LANG_STRING);

    Assert.assertTrue(Jack.getLookupFormatter().getName(jls).equals("Ljava/lang/String;"));
    Assert.assertTrue(jls ==
        session.getPhantomLookup().getClass(CommonTypes.JAVA_LANG_STRING));
    Assert.assertTrue(session.getLookup().getType(CommonTypes.JAVA_LANG_STRING) == jls);
    Assert.assertTrue(session.getLookup().getType("Ljava/lang/String;") == jls);
    Assert.assertTrue(session.getLookup().getClass(CommonTypes.JAVA_LANG_STRING) ==
        lookup.getClass("Ljava/lang/String;"));
  }

  @Test
  public void lookupJavaLangStringArray() {
    JType jls = lookup.getType("[[[Ljava/lang/String;");

    Assert.assertTrue(Jack.getLookupFormatter().getName(jls).equals("[[[Ljava/lang/String;"));
    Assert.assertTrue(session.getLookup()
        .getArrayType(session.getLookup().getType(CommonTypes.JAVA_LANG_STRING), 3) == jls);
  }

  @Test
  public void lookupJavaLangStringError1() {
    boolean fail = false;
    try {
      lookup.getInterface(CommonTypes.JAVA_LANG_STRING);
      fail = true;
    }
    catch (AssertionError e) {
      // Exception is normal path
    }
    Assert.assertFalse(fail);
  }

  @Test
  public void lookupJavaLangStringError2() {
    boolean fail = false;
    try {
      lookup.getType("java/lang/String;");
      fail = true;
    }
    catch (AssertionError e) {
      // Exception is normal path
    }
    Assert.assertFalse(fail);
  }

  @Test
  public void lookupTypeError1() {
    boolean fail = false;
    try {
      lookup.getType("L;");
      fail = true;
    }
    catch (AssertionError e) {
      // Exception is normal path
    }
    Assert.assertFalse(fail);
  }

  @Test
  public void lookupTypeError2() {
    boolean fail = false;
    try {
      lookup.getType("");
      fail = true;
    }
    catch (AssertionError e) {
      // Exception is normal path
    }
    Assert.assertFalse(fail);
  }

  @Test
  public void lookupTypeError3() {
    boolean fail = false;
    try {
      lookup.getType("L/C;");
      fail = true;
    }
    catch (AssertionError e) {
      // Exception is normal path
    }
    Assert.assertFalse(fail);
  }

  @Test
  public void lookupTypeError4() {
    boolean fail = false;
    try {
      lookup.getType("Lp/;");
      fail = true;
    }
    catch (AssertionError e) {
      // Exception is normal path
    }
    Assert.assertFalse(fail);
  }

  @Test
  public void lookupTypeError5() {
    boolean fail = false;
    try {
      lookup.getType("E");
      fail = true;
    }
    catch (AssertionError e) {
      // Exception is normal path
    }
    Assert.assertFalse(fail);
  }

  @Test
  public void lookupArrayError1() {
    boolean fail = false;
    try {
      lookup.getType("[[[[");
      fail = true;
    }
    catch (AssertionError e) {
      // Exception is normal path
    }
    Assert.assertFalse(fail);
  }

  @Test
  public void lookupMethodAppendLong() {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) lookup.getType("Ljava/lang/StringBuilder;");
    JMethod append = TestTools.getMethod(type, "append(J)Ljava/lang/StringBuilder;");

    Assert.assertTrue(append.getName().equals("append"));
  }

  @Test
  public void lookupMethodAppendString() {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) lookup.getType("Ljava/lang/StringBuilder;");
    JMethod append = TestTools.getMethod(type, "append(Ljava/lang/String;)Ljava/lang/StringBuilder;");

    Assert.assertTrue(append.getName().equals("append"));
    Assert.assertTrue(append.getParams().get(0).getType() ==
        session.getPhantomLookup().getClass(CommonTypes.JAVA_LANG_STRING));
  }

  @Test
  public void lookupConstructor() {
    JDefinedClassOrInterface type = (JDefinedClassOrInterface) lookup.getType("Ljava/lang/StringBuilder;");
    JMethod cons = type.getMethod("<init>", JPrimitiveTypeEnum.VOID.getType());

    Assert.assertTrue(cons instanceof JConstructor);
  }
}
