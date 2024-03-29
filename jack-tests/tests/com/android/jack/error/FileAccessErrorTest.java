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

package com.android.jack.error;

import com.android.jack.JackAbortException;
import com.android.jack.JackUserException;
import com.android.jack.Main;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.frontend.FrontendCompilationException;
import com.android.jack.library.LibraryIOException;
import com.android.jack.library.LibraryReadingException;
import com.android.jack.load.JackLoadingException;
import com.android.jack.test.helper.ErrorTestHelper;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchain;
import com.android.sched.util.config.PropertyIdException;
import com.android.sched.util.file.WrongPermissionException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;

/**
 * JUnit test checking Jack behavior on file access error.
 */
public class FileAccessErrorTest {

  @BeforeClass
  public static void setUpClass() {
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Checks that compilation fails correctly when folder to generate jack files is not readable.
   */
  @Test
  public void testFileAccessError001() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    AbstractTestTools.createFile(te.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    File jackOutputFile = AbstractTestTools.createTempDir();
    if (!jackOutputFile.setReadable(false)) {
      Assert.fail("Fails to change file permissions of " + jackOutputFile.getAbsolutePath());
    }
    JackApiToolchain jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    try {
      jackApiToolchain.srcToLib(
          AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
          jackOutputFile, /* zipFiles = */ false, te.getSourceFolder());
      Assert.fail();
    } catch (PropertyIdException e) {
      // Failure is ok since jack output folder is not readable
    } finally {
      if (!jackOutputFile.setReadable(true)) {
        Assert.fail("Fails to change file permissions of " + jackOutputFile.getAbsolutePath());
      }
    }
  }

  /**
   * Checks that compilation fails correctly when folder containing jack files is not readable.
   */
  @Test
  public void testFileAccessError002() throws Exception {
    ErrorTestHelper helper = new ErrorTestHelper();

    File srcFile = AbstractTestTools.createFile(helper.getSourceFolder(), "jack.incremental",
        "A.java", "package jack.incremental; \n" + "public class A {} \n");

    JackApiToolchain jackApiToolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    jackApiToolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
        helper.getJackFolder(), /* zipFiles = */ false, helper.getSourceFolder());

    AbstractTestTools.deleteFile(srcFile);

    srcFile = AbstractTestTools.createFile(helper.getSourceFolder(), "jack.incremental", "B.java",
        "package jack.incremental; \n" + "public class B extends A {} \n");

    // Modify read permission of folder containing jack files
    if (!helper.getJackFolder().setReadable(false)) {
      Assert.fail("Fails to change file permissions of " + helper.getJackFolder().getAbsolutePath());
    }

    jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackApiToolchain.setErrorStream(errOut);
    jackApiToolchain.addStaticLibs(helper.getJackFolder());
    try {
      jackApiToolchain.srcToExe(
          AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
          helper.getOutputDexFolder(), /* zipFile = */ false, helper.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      // Failure is ok since Jack file could not be imported since folder is not readable
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof WrongPermissionException);
    } finally {
      Assert.assertTrue("", errOut.toString().contains("is not readable"));
      if (!helper.getJackFolder().setReadable(true)) {
        Assert.fail("Fails to change file permissions of " + helper.getJackFolder().getAbsolutePath());
      }
    }
  }

  /**
   * Checks that compilation fails correctly when source file is not readable.
   */
  @Test
  public void testFileAccessError003() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    File a = AbstractTestTools.createFile(te.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");
    if (!a.setReadable(false)) {
      Assert.fail("Fails to change file permissions of " + a.getAbsolutePath());
    }

    JackApiToolchain jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    jackApiToolchain.setErrorStream(errOut);

    try {
      jackApiToolchain.srcToExe(
          AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
          te.getOutputDexFolder(), /* zipFile = */ false, te.getSourceFolder());
      Assert.fail();
    } catch (FrontendCompilationException e) {
      // Failure is ok since source file is not readable
    } finally {
      if (!a.setReadable(true)) {
        Assert.fail("Fails to change file permissions of " + a.getAbsolutePath());
      }
      Assert.assertTrue(errOut.toString().contains("Permission denied"));
    }
  }

  /**
   * Checks that compilation fails correctly when jack file is not readable.
   */
  @Test
  public void testFileAccessError004() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    AbstractTestTools.createFile(te.getSourceFolder(), "jack.incremental", "A.java",
        "package jack.incremental; \n"+
        "public class A {} \n");

    JackApiToolchain jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    jackApiToolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
        te.getJackFolder(), false, te.getSourceFolder());

    AbstractTestTools.deleteJavaFile(te.getSourceFolder(), "jack.incremental", "A.java");

    AbstractTestTools.createFile(te.getSourceFolder(),"jack.incremental", "B.java",
        "package jack.incremental; \n"+
        "public class B extends A {} \n");

    ByteArrayOutputStream errOut = new ByteArrayOutputStream();
    try {
      for (File jackFile : AbstractTestTools.getFiles(te.getJackFolder(), JayceFileImporter.JAYCE_FILE_EXTENSION)) {
        if (!jackFile.setReadable(false)) {
          Assert.fail("Fails to change file permissions of " + jackFile.getAbsolutePath());
        }
      }

      jackApiToolchain.setErrorStream(errOut);
      jackApiToolchain.srcToExe(
          AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath())
          + File.pathSeparator + te.getJackFolder().getAbsolutePath(),
          AbstractTestTools.createTempDir(), false, te.getSourceFolder());
      Assert.fail();
    } catch (JackAbortException e) {
      Assert.assertTrue(e.getCause() instanceof LibraryReadingException);
      Assert.assertTrue(e.getCause().getCause() instanceof LibraryIOException);
    } finally {
      String errOutput = errOut.toString();
      Assert.assertTrue(errOutput.contains("is an invalid library"));
      Assert.assertTrue(errOutput.contains("is not readable"));
      for (File jackFile : AbstractTestTools.getFiles(te.getJackFolder(), JayceFileImporter.JAYCE_FILE_EXTENSION)) {
        if (!jackFile.setReadable(true)) {
          Assert.fail("Fails to change file permissions of " + jackFile.getAbsolutePath());
        }
      }
    }
  }

  /**
   * Checks that compilation fails correctly when source file does not exist.
   */
  @Test
  public void testFileAccessError005() throws Exception {
    ErrorTestHelper te = new ErrorTestHelper();

    JackApiToolchain jackApiToolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    try {

      jackApiToolchain.srcToExe(
          AbstractTestTools.getClasspathAsString(jackApiToolchain.getDefaultBootClasspath()),
          te.getOutputDexFolder(), /* zipFile = */ false, new File(te.getSourceFolder(), "A.java"));

      Assert.fail();
    } catch (JackUserException e) {
      // Failure is ok since source file is not readable
      Assert.assertTrue(e.getMessage().contains("A.java is missing"));
    }
  }
}
