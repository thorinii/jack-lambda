/*
* Copyright (C) 2014 The Android Open Source Project
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

package com.android.jack.arithmetic;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ArithmeticTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.arithmetic.test001"),
    "com.android.jack.arithmetic.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.arithmetic.test002"),
    "com.android.jack.arithmetic.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.arithmetic.test003"),
    "com.android.jack.arithmetic.test003.dx.Tests");

  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.arithmetic.test004"),
    "com.android.jack.arithmetic.test004.dx.Tests");

  @BeforeClass
  public static void setUpClass() {
    ArithmeticTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }
  @Test
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test002() throws Exception {
    new RuntimeTestHelper(TEST002).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test003() throws Exception {
    new RuntimeTestHelper(TEST003).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test004() throws Exception {
    new RuntimeTestHelper(TEST004).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST003);
    rtTestInfos.add(TEST004);
  }
}
