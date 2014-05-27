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

package com.android.jack.errorhandling;

import com.android.jack.IllegalOptionsException;
import com.android.jack.Main;
import com.android.jack.NothingToDoException;
import com.android.jack.Options;
import com.android.jack.category.KnownBugs;
import com.android.jack.frontend.FrontendCompilationException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * JUnit test checking Jack behavior on exceptions.
 */
public class CommandLineErrorTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Checks that compilation fails correctly when an unsupported options is passed to ecj.
   */
  @Test
  public void testCommandLineError001() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add("-unsupported");
    ecjArgs.add(ite.getTestingFolder().getAbsolutePath());
    options.setEcjArguments(ecjArgs);

    try {
      ite.startErrRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (IllegalOptionsException e) {
      // Failure is ok since a bad options is passed to ecj.
    } finally {
      Assert.assertEquals("", ite.endErrRedirection());
    }
  }

  /**
   * Checks that compilation fails correctly when no source files are passed to ecj.
   */
  @Test
  public void testCommandLineError002() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    Options options = new Options();
    options.setEcjArguments(new ArrayList<String>());

    try {
      ite.startErrRedirection();
      ite.startOutRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (NothingToDoException e) {
      // Failure is ok since there is no source files.
    } finally {
      Assert.assertEquals("", ite.endErrRedirection());
      Assert.assertTrue(ite.endOutRedirection().contains("Usage:"));
    }
  }

  /**
   * Checks that compilation fails correctly when java.lang.Object does not exist on classpath.
   */
  @Test
  @Category(KnownBugs.class)
  public void testCommandLineError003() throws Exception {
    TestingEnvironment ite = new TestingEnvironment();

    File sourceFile = ite.addFile(ite.getSourceFolder(),"jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    Options options = new Options();
    List<String> ecjArgs = new ArrayList<String>();
    ecjArgs.add(sourceFile.getAbsolutePath());
    options.setEcjArguments(ecjArgs);

    try {
      ite.startErrRedirection();
      ite.startOutRedirection();
      ite.compile(options);
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Failure is ok, since java.lang.Object does not exists.
    } finally {
      Assert.assertEquals("", ite.endOutRedirection());
      Assert.assertTrue(!ite.endErrRedirection().contains("file"));
    }
  }
}