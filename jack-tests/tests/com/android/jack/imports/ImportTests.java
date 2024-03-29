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

package com.android.jack.imports;

import com.android.jack.backend.jayce.ImportConflictException;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.backend.jayce.TypeImportConflictException;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

public class ImportTests {

  @BeforeClass
  public static void setUpClass() {
    ImportTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  @Test
  public void testCompileNonConflictingSourceAndImport() throws Exception {
    File jackOut = AbstractTestTools.createTempDir();
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        jackOut,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.fibonacci.test001.jack"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addStaticLibs(jackOut);
    toolchain.srcToExe(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        AbstractTestTools.createTempDir(),
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.threeaddress.test001.jack"));
  }

  @Test
  public void testCompileConflictingSourceAndImport() throws Exception {
    File jackOut = AbstractTestTools.createTempDir();
    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        jackOut,
        /* zipFiles = */ false,
        AbstractTestTools.getTestRootDir("com.android.jack.fibonacci.test001.jack"));

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addStaticLibs(jackOut);
    try {
      toolchain.srcToExe(
          AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
          AbstractTestTools.createTempDir(),
          /* zipFile = */ false,
          AbstractTestTools.getTestRootDir("com.android.jack.fibonacci.test001.jack"));
      Assert.fail();
    } catch (ImportConflictException e) {
      // expected
    }
  }

  @Test
  public void testConflictingImport() throws Exception {
    String testName = "com.android.jack.inner.test015";
    File lib = AbstractTestTools.createTempDir();
    JackBasedToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        lib,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir(testName + ".lib"));


    toolchain = AbstractTestTools.getCandidateToolchain(JackBasedToolchain.class);
    toolchain.addStaticLibs(lib);
    toolchain.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), "keep-first");
    toolchain.srcToExe(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        AbstractTestTools.createTempFile("inner15", ".zip"),
        /* zipFile = */ true,
        AbstractTestTools.getTestRootDir(testName + ".jack"));
  }

  @Test
  public void testConflictingImportWithFailPolicy1() throws Exception {
    String testName = "com.android.jack.inner.test015";
    File lib = AbstractTestTools.createTempDir();
    JackApiToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        lib,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir(testName + ".lib"));

    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    // import twice the same lib
    toolchain.addStaticLibs(lib, lib);
    toolchain.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), "fail");
    try {
      toolchain.srcToExe(
          AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
          AbstractTestTools.createTempFile("inner15", ".zip"),
          /* zipFile = */ true,
          AbstractTestTools.getTestRootDir(testName + ".jack"));
      Assert.fail();
    } catch (TypeImportConflictException e) {
      // Exception is ok
    }
  }

  @Test
  public void testConflictingImportWithFailPolicy2() throws Exception {
    String testName = "com.android.jack.inner.test015";
    File lib1 = AbstractTestTools.createTempDir();
    JackApiToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        lib1,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir(testName + ".lib"));

    File lib2 = AbstractTestTools.createTempDir();
    toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        lib2,
        /* zipFile = */ false,
        AbstractTestTools.getTestRootDir(testName + ".lib"));

    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    // import twice the same lib
    toolchain.addStaticLibs(lib1, lib2);
    toolchain.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), "fail");

    try {
      toolchain.srcToExe(
          AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
          AbstractTestTools.createTempFile("inner15", ".zip"),
          /* zipFile = */ true,
          AbstractTestTools.getTestRootDir(testName + ".jack"));
      Assert.fail();
    } catch (TypeImportConflictException e) {
      // Exception is ok
    }
  }
}
