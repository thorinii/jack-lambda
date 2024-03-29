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

package com.android.jack.conditional;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

public class ConditionalTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.conditional.test001"),
    "com.android.jack.conditional.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.conditional.test002"),
    "com.android.jack.conditional.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.conditional.test003"),
    "com.android.jack.conditional.test003.dx.Tests");

  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.conditional.test004"),
    "com.android.jack.conditional.test004.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.conditional.test005"),
    "com.android.jack.conditional.test005.dx.Tests");

  private RuntimeTestInfo TEST006 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.conditional.test006"),
    "com.android.jack.conditional.test006.dx.Tests");

  private RuntimeTestInfo TEST007 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.conditional.test007"),
    "com.android.jack.conditional.test007.dx.Tests");

  @BeforeClass
  public static void setUpClass() {
    ConditionalTests.class.getClassLoader().setDefaultAssertionStatus(true);
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

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test005() throws Exception {
    new RuntimeTestHelper(TEST005).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test006() throws Exception {
    new RuntimeTestHelper(TEST006).compileAndRunTest();
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test007() throws Exception {
    new RuntimeTestHelper(TEST007).compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
    rtTestInfos.add(TEST002);
    rtTestInfos.add(TEST003);
    rtTestInfos.add(TEST004);
    rtTestInfos.add(TEST005);
    rtTestInfos.add(TEST006);
    rtTestInfos.add(TEST007);
  }
}
