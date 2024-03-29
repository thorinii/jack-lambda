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

package com.android.jack.bridge;

import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class BridgeTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.bridge.test001"),
    "com.android.jack.bridge.test001.dx.Tests");

  private RuntimeTestInfo TEST002 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.bridge.test002"),
    "com.android.jack.bridge.test002.dx.Tests");

  private RuntimeTestInfo TEST003 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.bridge.test003"),
    "com.android.jack.bridge.test003.dx.Tests");

  private RuntimeTestInfo TEST004 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.bridge.test004"),
    "com.android.jack.bridge.test004.dx.Tests");

  private RuntimeTestInfo TEST005 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.bridge.test005"),
    "com.android.jack.bridge.test005.dx.Tests");

  private RuntimeTestInfo TEST006 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.bridge.test006"),
    "com.android.jack.bridge.test006.dx.Tests");

  private RuntimeTestInfo TEST007 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.bridge.test007"),
    "com.android.jack.bridge.test007.dx.Tests");

  @BeforeClass
  public static void setUpClass() {
    BridgeTests.class.getClassLoader().setDefaultAssertionStatus(true);
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
  // TODO(jmhenaff): reintroduce ExtraTests category for this one?
  public void test007() throws Exception {
    new RuntimeTestHelper(TEST007).compileAndRunTest();
  }

  @Test
  public void test008() throws Exception {

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    File srcFolder = AbstractTestTools.getTestRootDir("com.android.jack.bridge.test008.jack");
    File jackZipOfGenericPackageProtected = AbstractTestTools.createTempFile("tmpBridge", ".zip");
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        jackZipOfGenericPackageProtected,
        /* zipFiles = */ true,
        srcFolder);

    // Build jack file from PublicExtendsGeneric.java
    toolchain = AbstractTestTools.getCandidateToolchain();
    File jackZipOfPublicExtendsGeneric = AbstractTestTools.createTempFile("tmpBridge", ".zip");
    toolchain.srcToLib(
        AbstractTestTools.getClasspathsAsString(toolchain.getDefaultBootClasspath(), new File [] {jackZipOfGenericPackageProtected}),
        jackZipOfPublicExtendsGeneric,
        /* zipFiles = */ true,
        new File(srcFolder, "sub/PublicExtendsGeneric.java"));

    // Build dex file representing Caller.java
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain = toolchain.addStaticLibs(jackZipOfPublicExtendsGeneric);
    File outDexFolder = AbstractTestTools.createTempDir();
    toolchain.srcToExe(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        outDexFolder,
        /* zipFile = */ false,
        new File(srcFolder, "Caller.java"));
  }

  @Test
  public void test009() throws Exception {
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToExe(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.bridge.test009"));
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
