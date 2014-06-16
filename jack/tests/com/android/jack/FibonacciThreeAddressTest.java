/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.jack;

import com.android.jack.dx.dex.file.ClassDefItem;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.ast.JSession;
import com.android.jack.scheduling.marker.DexFileMarker;
import com.android.jack.util.FileUtils;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;

/**
 * JUnit test for compilation of Fibonacci (three-address style).
 */
public class FibonacciThreeAddressTest {

  private static final File[] BOOTCLASSPATH = TestTools.getDefaultBootclasspath();

  private static final String CLASS_BINARY_NAME = "com/android/jack/fibonacci/jack/FibonacciThreeAddress";
  private static final String CLASS_SIGNATURE = "L" + CLASS_BINARY_NAME + ";";
  private static final String JAVA_FILENAME = "FibonacciThreeAddress.java";
  private static final File JAVA_FILEPATH = TestTools.getJackTestFromBinaryName(CLASS_BINARY_NAME);

  @BeforeClass
  public static void setUpClass() {
    // Enable assertions
    Main.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Verifies that FibonacciThreeAddress can be loaded in J-AST.
   */
  @Test
  public void testLoadFiboInJAst() throws Exception {
    JSession session = TestTools.buildJAst(TestTools.buildCommandLineArgs(JAVA_FILEPATH));
    Assert.assertNotNull(session);

    JDefinedClassOrInterface fibo = (JDefinedClassOrInterface) session.getLookup().getType(CLASS_SIGNATURE);
    Assert.assertNotNull(fibo);
  }

  /**
   * Verifies that FibonacciThreeAddress can be compiled into a {@code DexFile} containing
   * {@code ClassDefItem}.
   */
  @Test
  public void testBuildFiboDexFile() throws Exception {
    Options fiboArgs = TestTools.buildCommandLineArgs(JAVA_FILEPATH);
    fiboArgs.addProperty(Options.METHOD_FILTER.getName(), "reject-all-methods");
    JSession session = TestTools.buildSession(fiboArgs);

    JDefinedClassOrInterface fibo = (JDefinedClassOrInterface) session.getLookup().getType(CLASS_SIGNATURE);
    Assert.assertNotNull(fibo);

    DexFileMarker marker = session.getMarker(DexFileMarker.class);
    Assert.assertNotNull(marker);

    DexFile dexFile = marker.getDexFile();
    Assert.assertNotNull(dexFile);

    // Force dex preparation
    dexFile.prepare(null);

    // Check compiled class is present in the DexFile
    ClassDefItem fiboClassDefItem = dexFile.getClassOrNull(CLASS_BINARY_NAME);
    Assert.assertNotNull(fiboClassDefItem);

    // Check source file
    String sourceFilename = fiboClassDefItem.getSourceFile().getString();
    Assert.assertEquals(JAVA_FILENAME, sourceFilename);
  }

  /**
   * Compiles FibonacciThreeAddress into a {@code DexFile} and compares it to a dex file created
   * using a reference compiler and {@code dx}.
   * @throws Exception
   */
  @Test
  public void testCompareFiboDexFile() throws Exception {
    TestTools.checkStructure(BOOTCLASSPATH, null, JAVA_FILEPATH, false /*withDebugInfo*/);
  }

  /**
   * Verifies that FibonacciThreeAddress can be written to a dex file.
   */
  @Test
  public void testWriteFiboDexFile() throws Exception {

    File outputDir = FileUtils.getWorkingDirectory();
    Options fiboArgs = TestTools.buildCommandLineArgs(JAVA_FILEPATH);
    File outputFile = new File(outputDir, fiboArgs.getOutputFile().getName());
    File outputDirectory = outputFile.getParentFile();
    FileUtils.createIfNotExists(outputDirectory);

    Jack.run(fiboArgs);

    Assert.assertTrue(outputFile.exists());
    Assert.assertTrue(outputFile.length() > 0);
  }

  /**
   * Verifies that FibonacciThreeAddress can compiled from source to dex file.
   */
  @Test
  public void testCompileFibo() throws Exception {
    TestTools.runCompilation(TestTools.buildCommandLineArgs(JAVA_FILEPATH));
  }
}
