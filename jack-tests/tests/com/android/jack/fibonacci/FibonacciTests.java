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

package com.android.jack.fibonacci;

import com.android.jack.TestTools;
import com.android.jack.test.category.RuntimeRegressionTest;
import com.android.jack.test.comparator.ComparatorDex;
import com.android.jack.test.helper.CheckDexStructureTestHelper;
import com.android.jack.test.helper.RuntimeTestHelper;
import com.android.jack.test.helper.SourceToDexComparisonTestHelper;
import com.android.jack.test.runtime.RuntimeTest;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

public class FibonacciTests extends RuntimeTest {

  private RuntimeTestInfo TEST001 = new RuntimeTestInfo(
    AbstractTestTools.getTestRootDir("com.android.jack.fibonacci.test001"),
    "com.android.jack.fibonacci.test001.dx.Tests");

  @BeforeClass
  public static void setUpClass() {
    FibonacciTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  @Category(RuntimeRegressionTest.class)
  public void test001() throws Exception {
    new RuntimeTestHelper(TEST001).compileAndRunTest();
  }

  /**
   * Compiles FibonacciThreeAddress into a {@code DexFile} and compares it to a dex file created
   * using a reference compiler and {@code dx}.
   * @throws Exception
   */
  @Test
  public void testCompareFiboDexFile() throws Exception {
    SourceToDexComparisonTestHelper helper =
        new CheckDexStructureTestHelper(new File(TEST001.directory, "jack"));
    helper.runTest(new ComparatorDex(helper.getCandidateDex(), helper.getReferenceDex()));
  }

  @Override
  protected void fillRtTestInfos() {
    rtTestInfos.add(TEST001);
  }
}
