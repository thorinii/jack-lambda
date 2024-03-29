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

package com.android.jack.test.helper;

import com.google.common.base.CharMatcher;
import com.google.common.base.Splitter;
import com.google.common.io.Files;

import com.android.jack.test.runner.RuntimeRunner;
import com.android.jack.test.runtime.RuntimeTestInfo;
import com.android.jack.test.toolchain.AbstractTestTools;
import com.android.jack.test.toolchain.AndroidToolchain;
import com.android.jack.test.toolchain.Toolchain.SourceLevel;
import com.android.sched.util.collect.Lists;

import junit.framework.Assert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * This class is used to write runtime tests.
 */
public class RuntimeTestHelper {

  @Nonnull
  private List<File> baseDirs = new ArrayList<File>(1);
  @Nonnull
  private List<String> jUnitClasses = new ArrayList<String>(1);
  @Nonnull
  private List<File> referenceExtraSources = new ArrayList<File>(0);

  @Nonnull
  private String srcDirName = "jack";
  @Nonnull
  private String libDirName = "lib";
  @Nonnull
  private String refDirName = "dx";
  @Nonnull
  private String linkDirName = "link";

  private boolean withDebugInfos = false;

  private SourceLevel level = SourceLevel.JAVA_6;

  @Nonnull
  private List<FileChecker> testExeCheckers = new ArrayList<FileChecker>(0);

  @CheckForNull
  private String jarjarRulesFileName;
  @CheckForNull
  private String[] proguardFlagsFileNames;

  @Nonnull
  private String propertyFileName = "test.properties";

  public RuntimeTestHelper(@Nonnull RuntimeTestInfo... rtTestInfos) {
    for (RuntimeTestInfo info : rtTestInfos) {
      baseDirs.add(info.directory);
      jUnitClasses.add(info.jUnit);
    }
  }

  @Nonnull
  public RuntimeTestHelper setSrcDirName(@Nonnull String srcDirName) {
    this.srcDirName = srcDirName;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setLibDirName(@Nonnull String libDirName) {
    this.libDirName = libDirName;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setRefDirName(@Nonnull String refDirName) {
    this.refDirName = refDirName;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setLinkDirName(@Nonnull String linkDirName) {
    this.linkDirName = linkDirName;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setWithDebugInfos(boolean withDebugInfos) {
    this.withDebugInfos = withDebugInfos;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setSourceLevel(@Nonnull SourceLevel level) {
    this.level = level;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setPropertyFileName(@Nonnull String propertyFileName) {
    this.propertyFileName = propertyFileName;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setJarjarRulesFileName(@Nonnull String name) {
    this.jarjarRulesFileName = name;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper setProguardFlagsFileNames(@Nonnull String[] proguardFlagsFileNames) {
    this.proguardFlagsFileNames = proguardFlagsFileNames;
    return this;
  }

  @Nonnull
  public RuntimeTestHelper addTestExeFileChecker(@Nonnull FileChecker checker) {
    this.testExeCheckers.add(checker);
    return this;
  }

  @Nonnull
  public RuntimeTestHelper addReferenceExtraSources (@Nonnull File... extraSrc) {
    for (File file : extraSrc) {
      referenceExtraSources.add(file);
    }
    return this;
  }

  public void compileAndRunTest() throws Exception {
    compileAndRunTest(/* checkStructure = */ false);
  }

  public void compileAndRunTest(boolean checkStructure) throws Exception {
    Properties testProperties = new Properties();
    try {
      loadTestProperties(testProperties);
    } catch (FileNotFoundException e) {
      // No file, no pb
    }

//    AndroidToolchain candidateTestTools =
//        AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
//    AndroidToolchain referenceTestTools =
//        AbstractTestTools.getReferenceToolchain(AndroidToolchain.class);
//
//    candidateTestTools.setSourceLevel(level);
//    referenceTestTools.setSourceLevel(level);
//
//    candidateTestTools.setWithDebugInfos(withDebugInfos);
//    referenceTestTools.setWithDebugInfos(withDebugInfos);

    AndroidToolchain candidateTestTools = createCandidateToolchain();
    AndroidToolchain referenceTestTools = createReferenceToolchain();

    File[] candidateBootClasspath = candidateTestTools.getDefaultBootClasspath();
    File[] referenceBootClasspath = referenceTestTools.getDefaultBootClasspath();

    String candidateBootClasspathAsString =
        AbstractTestTools.getClasspathAsString(candidateTestTools.getDefaultBootClasspath());
    String referenceBootClasspathAsString =
        AbstractTestTools.getClasspathAsString(referenceTestTools.getDefaultBootClasspath());

    // Compile lib src
    File libLibRef = null;
    File libBinaryRef = null;
    File libLibCandidate = null;
    if (getLibSrc().length != 0) {
      libLibRef =
          AbstractTestTools.createTempFile("lib-ref", referenceTestTools.getLibraryExtension());
      File libBinaryRefDir = AbstractTestTools.createTempDir();
      libBinaryRef = new File(libBinaryRefDir, referenceTestTools.getBinaryFileName());
      referenceTestTools.srcToLib(referenceBootClasspathAsString, libLibRef, /* zipFiles = */ true,
          getLibSrc());
      referenceTestTools.libToExe(libLibRef, libBinaryRefDir, /* zipFile */ false);

      libLibCandidate = AbstractTestTools.createTempFile("lib-candidate",
          candidateTestTools.getLibraryExtension());
      candidateTestTools.srcToLib(candidateBootClasspathAsString, libLibCandidate,
      /* zipFiles = */ true, getLibSrc());
    }

    // Compile test src
    candidateTestTools = createCandidateToolchain();

    String candidateClasspathAsString;
    String referenceClasspathAsString;
    File[] candidateClassPath = candidateBootClasspath;
    if (getLibSrc().length != 0) {
      candidateClassPath = new File[candidateBootClasspath.length + 1];
      System.arraycopy(candidateBootClasspath, 0, candidateClassPath, 0,
          candidateBootClasspath.length);
      candidateClassPath[candidateClassPath.length - 1] = libLibCandidate;
      candidateClasspathAsString = AbstractTestTools.getClasspathAsString(candidateClassPath);
      File[] referenceClasspath = new File[referenceBootClasspath.length + 1];
      System.arraycopy(referenceBootClasspath, 0, referenceClasspath, 0,
          referenceBootClasspath.length);
      referenceClasspath[referenceClasspath.length - 1] = libLibRef;
      referenceClasspathAsString = AbstractTestTools.getClasspathAsString(referenceClasspath);
    } else {
      candidateClasspathAsString = candidateBootClasspathAsString;
      referenceClasspathAsString = referenceBootClasspathAsString;
    }

    File jarjarRules = getJarjarRules();
    List<File> proguargFlags = getProguardFlags();

    File testBinaryDir = AbstractTestTools.createTempDir();
    File testBinary = new File(testBinaryDir, candidateTestTools.getBinaryFileName());

    if (checkStructure) {
      CheckDexStructureTestHelper helper = new CheckDexStructureTestHelper(getSrcDir());
      helper.setCandidateClasspath(candidateClassPath);
      helper.setCandidateTestTools(candidateTestTools);
      if (jarjarRules != null) {
        helper.setJarjarRulesFile(jarjarRules);
      }
      helper.setProguardFlags(proguargFlags.toArray(new File[proguargFlags.size()]));
      helper.compare();
      Files.copy(helper.getCandidateDex(),
          new File(testBinaryDir, helper.getCandidateDex().getName()));
    } else {
      if (jarjarRules != null) {
        candidateTestTools.setJarjarRules(jarjarRules);
      }
      candidateTestTools.addProguardFlags(proguargFlags.toArray(new File [proguargFlags.size()]));
      candidateTestTools.srcToExe(candidateClasspathAsString, testBinaryDir, /* zipFile = */ false,
          getSrcDir());
    }

    for (FileChecker checker : testExeCheckers) {
      checker.check(testBinary);
    }

    referenceTestTools = createReferenceToolchain();
    File testLib =
        AbstractTestTools.createTempFile("testRef", referenceTestTools.getLibraryExtension());
    referenceTestTools.srcToLib(referenceClasspathAsString, testLib, /* zipFiles = */ true,
        getSrcDir());

    // Compile link src
    candidateTestTools = createCandidateToolchain();

    File linkBinary = null;
    if (getLinkSrc().length != 0) {
      File linkBinaryDir = AbstractTestTools.createTempDir();
      linkBinary = new File(linkBinaryDir, candidateTestTools.getBinaryFileName());
      candidateTestTools.setJarjarRules(jarjarRules);
      candidateTestTools.addProguardFlags(proguargFlags.toArray(new File [proguargFlags.size()]));
      candidateTestTools.srcToExe(candidateBootClasspathAsString, linkBinaryDir,
          /* zipFile = */ false, getLinkSrc());
    }

    // Compile ref part src
    referenceTestTools = createReferenceToolchain();

    List<File> referenceClasspath = new ArrayList<File>();
    for (File f : referenceBootClasspath) {
      referenceClasspath.add(f);
    }
    if (libLibRef != null) {
      referenceClasspath.add(libLibRef);
    }
    if (testLib != null) {
      referenceClasspath.add(testLib);
    }

    referenceClasspathAsString = AbstractTestTools.getClasspathAsString(
        referenceClasspath.toArray(new File[referenceClasspath.size()]));


    File [] refSources = getRefSrcDir();
//    File [] sources = new File [referenceExtraSources.size() + refSources.length];
    List<File> sources = new ArrayList<File>(referenceExtraSources.size() + refSources.length);
    sources = Lists.addAll(sources, refSources);
    sources = Lists.addAll(sources, referenceExtraSources);

    File refPartBinaryDir = AbstractTestTools.createTempDir();
//    File [] sources = new File [referenceExtraSources.size() + 1];
//    sources[0] = getRefSrcDir()[0];
//    for (int i = 1; i < sources.length; i++) {
//      sources[i] = referenceExtraSources.get(i - 1);
//    }
    File refPartBinary = new File(refPartBinaryDir, referenceTestTools.getBinaryFileName());
    referenceTestTools.srcToExe(
        referenceClasspathAsString,
        refPartBinaryDir,
        /* zipFile = */ false,
        sources.toArray(new File[sources.size()]));

    List<File> rtClasspath = new ArrayList<File>();
    rtClasspath.add(new File(AbstractTestTools.getJackRootDir(),
        "toolchain/jack/jack-tests/prebuilts/core-hostdex.jar"));
    rtClasspath.add(new File(AbstractTestTools.getJackRootDir(),
        "toolchain/jack/jack-tests/prebuilts/junit4-hostdex.jar"));
   if (refPartBinary != null) {
      rtClasspath.add(refPartBinary);
    }
    if (linkBinary != null) {
      rtClasspath.add(linkBinary);
    }
    if (testBinary != null) {
      rtClasspath.add(testBinary);
    }
    if (libBinaryRef != null) {
      rtClasspath.add(libBinaryRef);
    }

    // Run JUnit on runtime env(s)
    runOnRuntimeEnvironments(jUnitClasses, testProperties,
        rtClasspath.toArray(new File[rtClasspath.size()]));
  }

  @Nonnull
  private AndroidToolchain createCandidateToolchain() {
    AndroidToolchain candidateTestTools =
        AbstractTestTools.getCandidateToolchain(AndroidToolchain.class);
    candidateTestTools.setSourceLevel(level);
    candidateTestTools.setWithDebugInfos(withDebugInfos);
    return candidateTestTools;
  }

  @Nonnull
  private AndroidToolchain createReferenceToolchain() {
    AndroidToolchain referenceTestTools =
        AbstractTestTools.getReferenceToolchain(AndroidToolchain.class);
    referenceTestTools.setSourceLevel(level);
    referenceTestTools.setWithDebugInfos(withDebugInfos);
    return referenceTestTools;
  }

  private static void runOnRuntimeEnvironments(@Nonnull List<String> jUnitClasses,
      @Nonnull Properties testProperties, @Nonnull File... classpathFiles) throws Exception {
    List<RuntimeRunner> runnerList = AbstractTestTools.listRuntimeTestRunners(testProperties);
    String[] names = Lists.add(jUnitClasses, 0, AbstractTestTools.JUNIT_RUNNER_NAME).toArray(
        new String[jUnitClasses.size()]);
    for (RuntimeRunner runner : runnerList) {
      Assert.assertEquals(0, runner.run(
          getRuntimeArgs(runner.getClass().getSimpleName(), testProperties), names,
          classpathFiles));
    }
  }

  @Nonnull
  private static final String[] getRuntimeArgs(@Nonnull String rtName,
      @Nonnull Properties properties) {

    String args = properties.getProperty("rt.args." + rtName);
    List<String> result = new ArrayList<String>(0);
    if (args != null) {
      for (String arg : Splitter.on(CharMatcher.WHITESPACE).split(args)) {
        result.add(arg);
      }
    }
    return result.toArray(new String[result.size()]);
  }

  private void loadTestProperties(@Nonnull Properties properties) throws FileNotFoundException,
      IOException {
    File[] propertyFile = getDirectoryOrFile(propertyFileName);
    if (propertyFile.length != 0) {
      if (baseDirs.size() > 1) {
        throw new AssertionError("Non regression test found");
      }
      if (propertyFile[0].exists()) {
        properties.load(new FileInputStream(propertyFile[0]));
      }
    }
  }

  @Nonnull
  private File[] getSrcDir() {
    return getDirectoryOrFile(srcDirName);
  }

  @Nonnull
  private File[] getLibSrc() {
    return getDirectoryOrFile(libDirName);
  }

  @Nonnull
  private File[] getLinkSrc() {
    File[] result = getDirectoryOrFile(linkDirName);
    if (result.length != 0 && baseDirs.size() > 1) {
      throw new AssertionError("Not a regression test");
    }
    return result;
  }

  @Nonnull
  private File[] getRefSrcDir() {
    return getDirectoryOrFile(refDirName);
  }

  @CheckForNull
  private File getJarjarRules() {
    File[] result = getDirectoryOrFile(jarjarRulesFileName);
    if (result.length != 0) {
      if (baseDirs.size() > 1) {
        throw new AssertionError("Not a regression test");
      }
      return result[0];
    }
    return null;
  }

  @Nonnull
  private List<File> getProguardFlags() {
    List<File> result = new ArrayList<File>();
    if (proguardFlagsFileNames != null) {
      for (String s : proguardFlagsFileNames) {
        File[] f = getDirectoryOrFile(s);
        if (f.length != 0 && f[0].exists()) {
          result.add(f[0]);
        }
      }
    }

    if (!result.isEmpty() && baseDirs.size() > 1) {
      throw new AssertionError("Not a regression test");
    }

    return result;
  }

  @Nonnull
  private File[] getDirectoryOrFile(@CheckForNull String name) {
    List<File> result = new ArrayList<File>();
    if (name != null) {
      for (File f : baseDirs) {
        File absFile = new File(f, name);
        if (absFile.exists()) {
          result.add(absFile);
        }
      }
    }
    return result.toArray(new File[result.size()]);
  }
}
