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

package com.android.jack.compile.androidtree.core;

import com.android.jack.DexAnnotationsComparator;
import com.android.jack.DexComparator;
import com.android.jack.JarJarRules;
import com.android.jack.Options;
import com.android.jack.ProguardFlags;
import com.android.jack.TestTools;
import com.android.jack.backend.dex.DexFileWriter;
import com.android.jack.category.RedundantTests;
import com.android.jack.category.SlowTests;
import com.android.jack.config.id.JavaVersionPropertyId.JavaVersion;
import com.android.sched.vfs.Container;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import java.io.File;

@Ignore("Tree")
public class CoreCompilationTest {

  private static File SOURCELIST;

  @BeforeClass
  public static void setUpClass() {
    CoreCompilationTest.class.getClassLoader().setDefaultAssertionStatus(true);
    SOURCELIST = TestTools.getTargetLibSourcelist("core-libart");
  }

  @Test
  @Category(RedundantTests.class)
  public void compileCore() throws Exception {
    File outDexFolder = TestTools.createTempDir("core", ".dex");
    Options options = new Options();
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), JavaVersion.JAVA_7.toString());
    TestTools.compileSourceToDex(options, SOURCELIST, null, outDexFolder, false);
  }

  @Test
  public void compareLibCoreStructure() throws Exception {
    Options options = new Options();
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), JavaVersion.JAVA_7.toString());
    TestTools.checkStructure(options,
        /* bootclasspath = */ null,
        /* classpath = */ null,
        /* refBootclasspath = */ null,
        /* refClasspath = */ null,
        SOURCELIST,
        /* withDebugInfo = */ false,
        /* compareInstructionNumber = */ false,
        0.1f,
        (JarJarRules) null,
        (ProguardFlags[]) null);
  }

  @Test
  @Category(SlowTests.class)
  public void compileCoreWithJackAndDex() throws Exception {
    File coreDexFolderFromJava = TestTools.createTempDir("coreFromJava", "dex");
    File coreDexFromJava = new File(coreDexFolderFromJava, DexFileWriter.DEX_FILENAME);

    Options options = new Options();
    options.addProperty(Options.GENERATE_JACK_LIBRARY.getName(), "true");
    File outputFile = new File("/tmp/jackIncrementalOutput");
    options.addProperty(
        Options.DEX_OUTPUT_CONTAINER_TYPE.getName(), Container.DIR.toString());
    options.addProperty(Options.LIBRARY_OUTPUT_DIR.getName(), outputFile.getAbsolutePath());
    options.addProperty(
        Options.LIBRARY_OUTPUT_CONTAINER_TYPE.getName(), Container.DIR.toString());
    options.addProperty(Options.JAVA_SOURCE_VERSION.getName(), JavaVersion.JAVA_7.toString());
    TestTools.compileSourceToDex(options, SOURCELIST, null, coreDexFolderFromJava, false);

    File coreDexFolderFromJack = TestTools.createTempDir("coreFromJack", "dex");
    File coreDexFromJack = new File(coreDexFolderFromJack, DexFileWriter.DEX_FILENAME);
    TestTools.compileJackToDex(new Options(), outputFile, coreDexFolderFromJack,
        false);

    // Compare dex files structures and number of instructions
    new DexComparator(false /* withDebugInfo */, false /* strict */,
        false /* compareDebugInfoBinary */, true /* compareInstructionNumber */, 0).compare(
        coreDexFromJava, coreDexFromJack);
    new DexAnnotationsComparator().compare(coreDexFromJava, coreDexFromJack);
  }
}
