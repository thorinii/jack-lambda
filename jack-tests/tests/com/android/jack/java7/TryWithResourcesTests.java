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

package com.android.jack.java7;

import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit test for compilation of try-with-resources.
 */
public class TryWithResourcesTests {

  @BeforeClass
  public static void setUpClass() {
    TryWithResourcesTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void testCompile() throws Exception {
    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain();
    toolchain.setSourceLevel(SourceLevel.JAVA_7)
    .srcToExe(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.java7.trywithresources.test001.jack"));
  }

  /**
   * Verifies that the test source can compiled from source to dex file.
   */
  @Test
  public void testCompile2() throws Exception {
    IToolchain toolchain =
        AbstractTestTools.getCandidateToolchain();
    toolchain.setSourceLevel(SourceLevel.JAVA_7)
    .srcToExe(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.java7.trywithresources.test002.jack"));
  }
}
