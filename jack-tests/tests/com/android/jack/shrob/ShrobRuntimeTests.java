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

package com.android.jack.shrob;

import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.Test;

import java.io.File;

public class ShrobRuntimeTests extends RuntimeTest {

  private RuntimeTestInfo TEST011_1 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test011"),
      "com.android.jack.shrob.test011.dx.Tests");

  private RuntimeTestInfo TEST011_2 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test011"),
      "com.android.jack.shrob.test011.dx.Tests2");

  private RuntimeTestInfo TEST016 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test016"),
      "com.android.jack.shrob.test016.dx.Tests");

  private RuntimeTestInfo TEST025 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test025"),
      "com.android.jack.shrob.test025.dx.Tests");

  private RuntimeTestInfo TEST030 = new RuntimeTestInfo(
      AbstractTestTools.getTestRootDir("com.android.jack.shrob.test030"),
      "com.android.jack.shrob.test030.dx.Tests");

  @Test
  public void test011_1() throws Exception {
    new RuntimeTestHelper(TEST011_1)
    .setProguardFlagsFileNames(new String[] {"proguard.flags001", "../dontobfuscate.flags"})
    .compileAndRunTest();
  }

  @Test
  public void test011_2() throws Exception {
    new RuntimeTestHelper(TEST011_2)
    .setProguardFlagsFileNames(new String[] {"proguard.flags002"})
     .compileAndRunTest();
  }

  @Test
  public void test016() throws Exception {
    new RuntimeTestHelper(TEST016)
    .setProguardFlagsFileNames(new String[] {"proguard.flags001","applyMapping.flags"})
    .compileAndRunTest();
  }

  @Test
  public void test025() throws Exception {
    new RuntimeTestHelper(TEST025)
    .setProguardFlagsFileNames(new String[] {"proguard.flags001"})
    .compileAndRunTest();
  }

  @Test
  public void test030() throws Exception {
    new RuntimeTestHelper(TEST030)
    .setProguardFlagsFileNames(new String[] {new File(TEST030.directory, "proguard.flags001")
        .getAbsolutePath()})
    .compileAndRunTest();
  }

  @Override
  protected void fillRtTestInfos() {
  }

}
