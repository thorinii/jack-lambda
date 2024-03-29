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

package com.android.jack.multidex;

import com.android.jack.JackAbortException;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.backend.dex.DexWritingException;
import com.android.jack.backend.dex.MainDexOverflowException;
import com.android.jack.backend.dex.MultiDexLegacy;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.JackApiToolchain;
import com.android.jack.tools.merger.FieldIdOverflowException;
import com.android.jack.tools.merger.MethodIdOverflowException;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

public class MultiDexOverflowTests {

  private static File annotations;

  @Nonnull
  private static final String EXPECTED_MESSAGE =
      "Error during the dex writing phase: classes.dex has too many IDs";

  @BeforeClass
  public static void init() throws IOException, Exception {
    annotations = MultiDexTests.prepareAnnotations();
  }

  @Test
  public void testMinimalMainDexOverflowWithMethods() throws Exception {
    File srcFolder = TestTools.createTempDir("src", "dir");
    File outFolder = TestTools.createTempDir("out", "dir");

    int fileCount = 655;
    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithMethods(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithMethods(srcFolder, fileCount, 36);

    JackApiToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    toolchain.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "minimal-multidex");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    toolchain.setErrorStream(baos);

    try {
    toolchain.srcToExe(
        AbstractTestTools.getClasspathsAsString(
            toolchain.getDefaultBootClasspath(),
            new File[] {annotations}),
        outFolder,
        /* zipFile = */ false,
        srcFolder);
      Assert.fail();
    } catch (JackAbortException e) {
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof DexWritingException);
      Assert.assertTrue(cause.getCause() instanceof MainDexOverflowException);
      Assert.assertTrue(cause.getCause().getCause() instanceof MethodIdOverflowException);
      Assert.assertTrue(baos.toString().contains(EXPECTED_MESSAGE));
    }
  }

  @Test
  public void testStandardMainDexOverflowWithFields() throws Exception {
    File srcFolder = TestTools.createTempDir("src", "dir");
    File outFolder = TestTools.createTempDir("out", "dir");

    int fileCount = 655;
    for (int fileIdx = 0; fileIdx < fileCount; fileIdx++) {
      generateJavaFileWithFields(srcFolder, fileIdx, 100);
    }
    generateJavaFileWithFields(srcFolder, fileCount, 37);

    JackApiToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);

    toolchain.addProperty(MultiDexLegacy.MULTIDEX_LEGACY.getName(), "true");
    toolchain.addProperty(DexFileWriter.DEX_WRITING_POLICY.getName(), "multidex");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    toolchain.setErrorStream(baos);

    try {
    toolchain.srcToExe(
        AbstractTestTools.getClasspathsAsString(
            toolchain.getDefaultBootClasspath(),
            new File[] {annotations}),
        outFolder,
        /* zipFile = */ false,
        srcFolder);
      Assert.fail();
    } catch (JackAbortException e) {
      Throwable cause = e.getCause();
      Assert.assertTrue(cause instanceof DexWritingException);
      Assert.assertTrue(cause.getCause() instanceof MainDexOverflowException);
      Assert.assertTrue(cause.getCause().getCause() instanceof FieldIdOverflowException);
      Assert.assertTrue(baos.toString().contains(EXPECTED_MESSAGE));
    }
  }

  private void generateJavaFileWithMethods(@Nonnull File srcFolder, @Nonnegative int fileIdx,
      @Nonnegative int methodCount) throws IOException, FileNotFoundException {
    File javaFile = new File(srcFolder, "A" + fileIdx + ".java");
    if (!javaFile.createNewFile()) {
      throw new IOException("Failed to create file " + javaFile.getAbsolutePath());
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(javaFile);
      StringBuilder content = new StringBuilder("package jack.merger; \n"
          + "@com.android.jack.annotations.ForceInMainDex public class A" + fileIdx + " {\n");
      // -1 due to implicit init method
      for (int mthIdx = 0; mthIdx < methodCount - 1; mthIdx++) {
        content.append("public void m" + mthIdx + "() {} \n");
      }
      content.append("} \n");
      fos.write(content.toString().getBytes());
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }

  private void generateJavaFileWithFields(@Nonnull File srcFolder, @Nonnegative int fileIdx,
      @Nonnegative int fieldCount) throws IOException, FileNotFoundException {
    File javaFile = new File(srcFolder, "A" + fileIdx + ".java");
    if (!javaFile.createNewFile()) {
      throw new IOException("Failed to create file " + javaFile.getAbsolutePath());
    }
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(javaFile);
      StringBuilder content = new StringBuilder("package jack.merger; \n"
          + "@com.android.jack.annotations.ForceInMainDex public class A" + fileIdx + " {\n");
      for (int fieldIdx = 0; fieldIdx < fieldCount; fieldIdx++) {
        content.append("public int f" + fieldIdx + ";\n");
      }
      content.append("} \n");
      fos.write(content.toString().getBytes());
    } finally {
      if (fos != null) {
        fos.close();
      }
    }
  }

}
