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

package com.android.jack.fileconflict;

import com.android.jack.Jack;
import com.android.jack.Options;
import com.android.jack.ProguardFlags;
import com.android.jack.TestTools;
import com.android.jack.backend.jayce.ImportConflictException;
import com.android.jack.backend.jayce.JayceFileImporter;
import com.android.jack.backend.jayce.ResourceImportConflictException;
import com.android.jack.test.category.KnownBugs;
import com.android.jack.library.FileType;
import com.android.jack.library.JackLibrary;
import com.android.jack.shrob.obfuscation.NameProviderFactory;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.IToolchain;
import com.android.jack.test.toolchain.JackApiToolchain;
import com.android.jack.test.toolchain.JackBasedToolchain;
import com.android.sched.util.stream.ByteStreamSucker;

import junit.framework.Assert;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * JUnit tests for resource support.
 */
public class FileConflictTests {

  @Nonnull
  private static final String COMMON_PATH_001 = "com/android/jack/fileconflict/test001/jack/";
  @Nonnull
  private static final String COMMON_PATH_002 = "com/android/jack/fileconflict/test002/jack/";
  @Nonnull
  private static final String JACK_FILE_PATH_1 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_001 + "MyClass.jayce";
  @Nonnull
  private static final String JACK_FILE_PATH_2 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_001 + "MyClass2.jayce";
  @Nonnull
  private static final String JACK_FILE_PATH_3 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_001 + "MyClass3.jayce";
  @Nonnull
  private static final String DEX_FILE_PATH_1 =
      FileType.DEX.getPrefix() + "/" + COMMON_PATH_001 + "MyClass.dex";
  @Nonnull
  private static final String DEX_FILE_PATH_2 =
      FileType.DEX.getPrefix() + "/" + COMMON_PATH_001 + "MyClass2.dex";
  @Nonnull
  private static final String DEX_FILE_PATH_3 =
      FileType.DEX.getPrefix() + "/" + COMMON_PATH_001 + "MyClass3.dex";
  @Nonnull
  private static final String JACK_FILE_PATH_002_1 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_002 + "IrrelevantForTest.jayce";
  @Nonnull
  private static final String JACK_FILE_PATH_002_2 =
      FileType.JAYCE.getPrefix() + "/" + COMMON_PATH_002 + "IrrelevantForTest2.jayce";
  @Nonnull
  private static final String RESOURCE1_SHORTPATH = "Resource1";
  @Nonnull
  private static final String RESOURCE2_SHORTPATH = "Resource2";
  @Nonnull
  private static final String RESOURCE3_SHORTPATH = "Resource3";
  @Nonnull
  private static final String RESOURCE1_LONGPATH = FileType.RSC.getPrefix() + "/"
      + RESOURCE1_SHORTPATH;
  @Nonnull
  private static final String RESOURCE2_LONGPATH = FileType.RSC.getPrefix() + "/"
      + RESOURCE2_SHORTPATH;
  @Nonnull
  private static final String RESOURCE3_LONGPATH = FileType.RSC.getPrefix() + "/"
      + RESOURCE3_SHORTPATH;

  @Nonnull
  private static final File TEST001_DIR =
      AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test001.jack");

  @Nonnull
  private static final File TEST002_DIR =
      AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test002.jack");

  @BeforeClass
  public static void setUpClass() {
    FileConflictTests.class.getClassLoader().setDefaultAssertionStatus(true);
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with no collision policy specified.
   * @throws Exception
   */
  @Test
  public void test001a() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    try {
      runTest001(jackOutput, null);
      Assert.fail();
    } catch (ImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with the collision policy set to "fail".
   * @throws Exception
   */
  @Test
  public void test001b() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    try {
      runTest001(jackOutput, "fail");
      Assert.fail();
    } catch (ImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting Jack files, and
   * outputting to a Jack folder, with the collision policy set to "keep-first".
   * @throws Exception
   */
  @Test
  public void test001c() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    runTest001(jackOutput, "keep-first");
    File myClass1 = new File(jackOutput, JACK_FILE_PATH_1);
    File myClass2 = new File(jackOutput, JACK_FILE_PATH_2);
    File myClass3 = new File(jackOutput, JACK_FILE_PATH_3);
    Assert.assertTrue(myClass1.exists());
    Assert.assertTrue(myClass2.exists());
    Assert.assertTrue(myClass3.exists());
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack folder, with no collision policy specified.
   * @throws Exception
   */
  @Test
  public void test002a() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    try {
      runTest002(jackOutput, false /* non-zipped */, null);
      Assert.fail();
    } catch (ResourceImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack folder, with the collision policy set to "fail".
   * @throws Exception
   */
  @Test
  public void test002b() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    try {
      runTest002(jackOutput, false /* non-zipped */, "fail");
      Assert.fail();
    } catch (ResourceImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack folder, with the collision policy set to "keep-first".
   * @throws Exception
   */
  @Test
  public void test002c() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();
    runTest002(jackOutput, false /* non-zipped */, "keep-first");
    checkResourceContent(jackOutput, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(jackOutput, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(jackOutput, RESOURCE3_LONGPATH, "Res3");
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with no collision policy specified.
   * @throws Exception
   */
  @Test
  public void test002d() throws Exception {
    File jackOutput = AbstractTestTools.createTempFile("jackoutput", ".zip");
    try {
      runTest002(jackOutput, true /* zipped */, null);
      Assert.fail();
    } catch (ResourceImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with the collision policy set to "fail".
   * @throws Exception
   */
  @Test
  public void test002e() throws Exception {
    File jackOutput = AbstractTestTools.createTempFile("jackoutput", ".zip");
    try {
      runTest002(jackOutput, true /* zipped */, "fail");
      Assert.fail();
    } catch (ResourceImportConflictException e) {
    }
  }

  /**
   * Test the behavior of Jack when importing 2 Jack folders containing conflicting resources, and
   * outputting to a Jack zip, with the collision policy set to "keep-first".
   * @throws Exception
   */
  @Test
  public void test002f() throws Exception {
    File jackOutput = AbstractTestTools.createTempFile("jackoutput", ".zip");
    runTest002(jackOutput, true /* zipped */, "keep-first");
    ZipFile zipFile = new ZipFile(jackOutput);
    checkResourceContent(zipFile, RESOURCE1_LONGPATH, "Res1");
    checkResourceContent(zipFile, RESOURCE2_LONGPATH, "Res2");
    checkResourceContent(zipFile, RESOURCE3_LONGPATH, "Res3");
  }

  /**
   * Test the behavior of Jack when outputting a Jack file to a Jack folder where a Jack file of the
   * same name already exists. We expect the previous file to be overridden.
   * @throws Exception
   */
  @Test
  public void test003a() throws Exception {
    // compile source files to a Jack dir
    File jackOutput = AbstractTestTools.createTempDir();
    File testSrcDir = AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test003");
    File tempJackFolder = AbstractTestTools.createTempDir();

    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        tempJackFolder,
        /* zipFiles = */ false,
        testSrcDir);

    // get paths for Jack files
    String jackFilePath =
        FileType.JAYCE.getPrefix() + "/com/android/jack/fileconflict/test003/jack/MyClass.jayce";
    File myClass1 = new File(tempJackFolder, jackFilePath);

    // get paths for Dex files
    String dexFilePath =
        FileType.DEX.getPrefix() + "/com/android/jack/fileconflict/test003/jack/MyClass.dex";
    File myClass1Dex = new File(tempJackFolder, dexFilePath);

    String libPropName = JackLibrary.LIBRARY_PROPERTIES_VPATH.getPathAsString('/');
    File libProperties = new File(tempJackFolder, libPropName);

    // create Jack dirs to import
    File jackImport1 = AbstractTestTools.createTempDir();
    copyFileToDir(libProperties, libPropName, jackImport1);
    copyFileToDir(myClass1, jackFilePath, jackImport1);
    copyFileToDir(myClass1Dex, dexFilePath, jackImport1);

    // copy Jack file to output dir
    copyFileToDir(myClass1, jackFilePath, jackOutput);
    copyFileToDir(myClass1Dex, dexFilePath, jackOutput);

    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.addProguardFlags(new File(testSrcDir, "proguard.flags"));
    toolchain.libToLib(jackImport1, jackOutput, false);
  }

  /**
   * Test the behavior of Jack when outputting a resource to a Jack folder where a file of the
   * same name already exists. We expect the previous file to be overridden.
   * @throws Exception
   */
  @Test
  @Ignore("Now jack generate library, a previous file can not exists")
  public void test003b() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();

    // compile source files to a Jack dir
    File testSrcDir = AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test003.jack");
    File tempJackFolder = AbstractTestTools.createTempDir();
    IToolchain toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        tempJackFolder,
        /* zipFiles = */ false,
        testSrcDir);

    // get paths for Jack files
    String jackFilePath =
        FileType.JAYCE.getPrefix() + "/com/android/jack/fileconflict/test003/jack/MyClass.jayce";
    File myClass1 = new File(tempJackFolder, jackFilePath);

    String libPropName = JackLibrary.LIBRARY_PROPERTIES_VPATH.getPathAsString('/');
    File libProperties = new File(tempJackFolder, libPropName);

    // create Jack dirs to import
    File jackImport1 = AbstractTestTools.createTempDir();
    String resourcePath = "com/android/jack/fileconflict/test003/jack/Resource";
    File resource = new File(testSrcDir, "Resource");
    copyFileToDir(libProperties, libPropName, jackImport1);
    copyFileToDir(myClass1, jackFilePath, jackImport1);
    copyFileToDir(resource, resourcePath, jackImport1);

    // copy a different resource to output dir with the same name
    File resource2 = new File(testSrcDir, "Resource2");
    copyFileToDir(resource2, resourcePath, jackOutput);

    // run Jack on Jack dir
    toolchain = AbstractTestTools.getCandidateToolchain();
    toolchain.libToLib(jackImport1, jackOutput, /* zipFiles = */ false);

    checkResourceContent(jackOutput, resourcePath, "Res1");

  }

  /**
   * Test the behavior of Jack when renaming a Jack file along with the resource with a matching
   * name, and when a resource with the same name (after renaming) already exists.
   * @throws Exception
   */
  @Test
  @Category(KnownBugs.class)
  public void test004() throws Exception {
    File jackOutput = AbstractTestTools.createTempDir();

    // compile source files to a Jack dir
    File testSrcDir = AbstractTestTools.getTestRootDir("com.android.jack.fileconflict.test004.jack");
    File tempJackFolder = AbstractTestTools.createTempDir();

    JackApiToolchain toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        tempJackFolder,
        /* zipFiles = */ false,
        testSrcDir);

    // get paths for Jack files
    String jackFilePath =
        FileType.JAYCE.getPrefix() + "/com/android/jack/fileconflict/test004/jack/MyClass.jayce";
    File myClass1 = new File(tempJackFolder, jackFilePath);

    String libPropName = JackLibrary.LIBRARY_PROPERTIES_VPATH.getPathAsString('/');
    File libProperties = new File(tempJackFolder, libPropName);

    // create Jack dirs to import
    File jackImport1 = AbstractTestTools.createTempDir();
    File resource = new File(testSrcDir, "MyClass.txt");
    copyFileToDir(libProperties, libPropName, jackImport1);
    copyFileToDir(myClass1, jackFilePath, jackImport1);
    copyFileToDir(resource, "com/android/jack/fileconflict/test004/jack/MyClass.txt", jackImport1);
    System.out.println(jackImport1.getAbsolutePath());

    // copy a different resource to output dir with the same name
    File resource2 = new File(testSrcDir, "a.txt");
    copyFileToDir(resource2, "pcz/nbqfcvq/wnpx/svyrpcbsyvph/hrgh004/wnpx/ZmPyngg.txt", jackOutput);

    // run Jack on Jack dir
    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.addProguardFlags(new File(testSrcDir, "proguard.flags"));
    toolchain.addProperty(NameProviderFactory.NAMEPROVIDER.getName(), "rot13");
    toolchain.libToLib(jackImport1, jackOutput, /* zipFiles = */ false);

    checkResourceContent(jackOutput, "pcz/nbqfcvq/wnpx/svyrpcbsyvph/hrgh004/wnpx/ZmPyngg.txt",
        "MyClass");
  }

  private void runTest001(@Nonnull File jackOutput, @CheckForNull String collisionPolicy)
      throws Exception {
    // compile source files to a Jack dir
    File tempJackFolder = AbstractTestTools.createTempDir();
    JackApiToolchain toolchain =
        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.srcToLib(
        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
        tempJackFolder,
        /* zipFile = */ false,
        TEST001_DIR);

    // get paths for Jack files
    File myClass1 = new File(tempJackFolder, JACK_FILE_PATH_1);
    File myClass2 = new File(tempJackFolder, JACK_FILE_PATH_2);
    File myClass3 = new File(tempJackFolder, JACK_FILE_PATH_3);

    // get paths for dex files
    File myClass1Dex = new File(tempJackFolder, DEX_FILE_PATH_1);
    File myClass2Dex = new File(tempJackFolder, DEX_FILE_PATH_2);
    File myClass3Dex = new File(tempJackFolder, DEX_FILE_PATH_3);

    String libPropName = JackLibrary.LIBRARY_PROPERTIES_VPATH.getPathAsString('/');
    File libProperties = new File(tempJackFolder, libPropName);

    // create Jack dirs to import
    File jackImport1 = AbstractTestTools.createTempDir();
    File jackImport2 = AbstractTestTools.createTempDir();
    copyFileToDir(libProperties, libPropName, jackImport1);
    copyFileToDir(myClass1, JACK_FILE_PATH_1, jackImport1);
    copyFileToDir(myClass1Dex, DEX_FILE_PATH_1, jackImport1);
    copyFileToDir(myClass2, JACK_FILE_PATH_2, jackImport1);
    copyFileToDir(myClass2Dex, DEX_FILE_PATH_2, jackImport1);
    copyFileToDir(libProperties, libPropName, jackImport2);
    copyFileToDir(myClass1, JACK_FILE_PATH_1, jackImport2);
    copyFileToDir(myClass1Dex, DEX_FILE_PATH_1, jackImport2);
    copyFileToDir(myClass3, JACK_FILE_PATH_3, jackImport2);
    copyFileToDir(myClass3Dex, DEX_FILE_PATH_3, jackImport2);

    // run Jack on Jack dirs
    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
    toolchain.addProguardFlags(new File(TEST001_DIR, "proguard.flags"));
    toolchain.addStaticLibs(jackImport1, jackImport2);
    if (collisionPolicy != null) {
      toolchain.addProperty(JayceFileImporter.COLLISION_POLICY.getName(), collisionPolicy);
    }
    toolchain.libToLib(new File [] {jackImport1, jackImport2}, jackOutput, /* zipFiles = */ false);
  }

  private void runTest002(@Nonnull File jackOutput, boolean zip,
      @CheckForNull String collisionPolicy) throws Exception {
    // compile source files to a Jack dir
    File jackImport1 = TestTools.createTempDir("jack", "dir");
    Options options = new Options();
    File lib1 = new File(TEST002_DIR, "lib1");
    options.addResource(new File(lib1, "rsc"));
    TestTools.compileSourceToJack(options, lib1, TestTools.getDefaultBootclasspathString(),
        jackImport1, false /* non-zipped */);

    File jackImport2 = TestTools.createTempDir("jack", "dir");
    options = new Options();
    File lib2 = new File(TEST002_DIR, "lib2");
    options.addResource(new File(lib2, "rsc"));
    TestTools.compileSourceToJack(options, lib2, TestTools.getDefaultBootclasspathString(),
        jackImport2, false /* non-zipped */);

    // run Jack on Jack dirs
    ProguardFlags flags = new ProguardFlags(new File(TEST002_DIR, "proguard.flags"));
    options = new Options();
    List<File> jayceImports = new ArrayList<File>(2);
    jayceImports.add(jackImport1);
    jayceImports.add(jackImport2);
    options.setJayceImports(jayceImports);
    options.setProguardFlagsFile(Collections.<File>singletonList(flags));
    if (zip) {
      options.setJayceOutputZip(jackOutput);
    } else {
      options.setJayceOutputDir(jackOutput);
    }
    if (collisionPolicy != null) {
      options.addProperty(JayceFileImporter.RESOURCE_COLLISION_POLICY.getName(), collisionPolicy);
    }
    Jack.run(options);
  }

//  private void runTest002(@Nonnull File jackOutput, boolean zip,
//      @CheckForNull String collisionPolicy) throws Exception {
//    // compile source files to a Jack dir
//    // TODO(jmhenaff): adapt this when resources are added to toolchain APIs
//    File jackImport1 = TestTools.createTempDir("jack", "dir");
//    Options options = new Options();
//    File lib1 = new File(TEST002_DIR, "lib1");
//    options.addResource(new File(lib1, "rsc"));
//    TestTools.compileSourceToJack(options, lib1, TestTools.getDefaultBootclasspathString(),
//        jackImport1, false /* non-zipped */);
//
//    File jackImport2 = TestTools.createTempDir("jack", "dir");
//    options = new Options();
//    File lib2 = new File(TEST002_DIR, "lib2");
//    options.addResource(new File(lib2, "rsc"));
//    TestTools.compileSourceToJack(options, lib2, TestTools.getDefaultBootclasspathString(),
//        jackImport2, false /* non-zipped */);
////    File tempJackFolder = AbstractTestTools.createTempDir();
////    JackApiToolchain toolchain =
////        AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
////    toolchain.srcToLib(
////        AbstractTestTools.getClasspathAsString(toolchain.getDefaultBootClasspath()),
////        tempJackFolder,
////        /* zipFile = */ false,
////        TEST002_DIR);
////
////    // get paths for Jack files
////    File myClass1 = new File(tempJackFolder, JACK_FILE_PATH_002_1);
////    File myClass2 = new File(tempJackFolder, JACK_FILE_PATH_002_2);
////
////    // get paths for resources
////    File resource1 = new File(TEST002_DIR, RESOURCE1_SHORTPATH);
////    File resource2 = new File(TEST002_DIR, RESOURCE2_SHORTPATH);
////    File resource3 = new File(TEST002_DIR, RESOURCE3_SHORTPATH);
////
////    // create Jack dirs to import
////    File jackImport1 = AbstractTestTools.createTempDir();
////    File jackImport2 = AbstractTestTools.createTempDir();
////    copyFileToDir(myClass1, JACK_FILE_PATH_002_1, jackImport1);
////    copyFileToDir(resource1, RESOURCE1_LONGPATH, jackImport1);
////    copyFileToDir(resource2, RESOURCE2_LONGPATH, jackImport1);
////    copyFileToDir(myClass2, JACK_FILE_PATH_002_2, jackImport2);
////    copyFileToDir(resource2, RESOURCE1_LONGPATH, jackImport2);
////    copyFileToDir(resource3, RESOURCE3_LONGPATH, jackImport2);
//
//    // run Jack on Jack dirs
//    toolchain = AbstractTestTools.getCandidateToolchain(JackApiToolchain.class);
//    toolchain.addProguardFlags(new File(TEST002_DIR, "proguard.flags"));
//    if (collisionPolicy != null) {
//      toolchain.addProperty(JayceFileImporter.RESOURCE_COLLISION_POLICY.getName(), collisionPolicy);
//    }
//    toolchain.libToLib(new File [] {jackImport1, jackImport2},  jackOutput, zip);
//
//  }

  private void copyFileToDir(@Nonnull File fileToCopy, @Nonnull String relativePath,
      @Nonnull File dir) throws IOException {
    FileOutputStream fos = null;
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(fileToCopy);
      File copiedFile = new File(dir, relativePath);
      File parentDir = copiedFile.getParentFile();
      if (!parentDir.exists()) {
        boolean res = parentDir.mkdirs();
        if (!res) {
          throw new AssertionError();
        }
      }
      try {
        fos = new FileOutputStream(copiedFile);
        ByteStreamSucker sucker = new ByteStreamSucker(fis, fos);
        sucker.suck();
      } finally {
        if (fos != null) {
          fos.close();
        }
      }
    } finally {
      if (fis != null) {
        fis.close();
      }
    }
  }

  private void checkResourceContent(@Nonnull File dir, @Nonnull String path,
      @Nonnull String expectedContent) throws IOException {
    assert dir.isDirectory();
    File file = new File(dir, path);
    Assert.assertTrue(file.exists());
    BufferedReader reader = null;
    try {
      InputStream in = new FileInputStream(file);
      reader = new BufferedReader(new InputStreamReader(in));
      String line = reader.readLine();
      Assert.assertEquals(expectedContent, line);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }

  private void checkResourceContent(@Nonnull ZipFile zipFile, @Nonnull String entryName,
      @Nonnull String expectedContent) throws IOException {
    ZipEntry entry = zipFile.getEntry(entryName);
    Assert.assertNotNull(entry);
    BufferedReader reader = null;
    try {
      InputStream in = zipFile.getInputStream(entry);
      reader = new BufferedReader(new InputStreamReader(in));
      String line = reader.readLine();
      Assert.assertEquals(expectedContent, line);
    } finally {
      if (reader != null) {
        reader.close();
      }
    }
  }
}
