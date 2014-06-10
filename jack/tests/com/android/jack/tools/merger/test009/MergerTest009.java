/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"));
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

package com.android.jack.tools.merger.test009;

import com.android.jack.Main;
import com.android.jack.TestTools;
import com.android.jack.category.SlowTests;
import com.android.jack.tools.merger.MergerTestTools;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;


/**
 * JUnit test checking that merging of core works.
 */
public class MergerTest009 extends MergerTestTools {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Ignore("Tree")
  @Test
  @Category(SlowTests.class)
  public void testMergerOnCore() throws Exception {
    Assert
        .assertFalse(compareMonoDexWithOneDexPerType(TestTools.getTargetLibSourcelist("core"),
            false /* withDebug */));
  }
}
