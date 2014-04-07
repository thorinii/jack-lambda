/*
 * Copyright (C) 2013 The Android Open Source Project
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

package com.android.jack.backend.jayce;

import com.android.jack.Jack;
import com.android.jack.JackFileException;
import com.android.jack.Options;
import com.android.jack.ir.JackFormatIr;
import com.android.jack.ir.NonJackFormatIr;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.ir.formatter.TypeFormatter;
import com.android.jack.jayce.JayceWriter;
import com.android.jack.scheduling.feature.JackFileNonZipOutput;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Support;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.file.Directory;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.direct.OutputDirectDir;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Writer of Jayce files in a folder organized according to package names.
 */
@Description("Writer of Jayce files in a folder organized according to package names")
@Name("JayceSingleTypeWriter")
@Constraint(need = {JackFormatIr.class}, no = {NonJackFormatIr.class})
@Produce(JackFormatProduct.class)
@Support(JackFileNonZipOutput.class)
@Synchronized
public class JayceSingleTypeWriter implements RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private static final TypeFormatter formatter = new FilePathFormatter();

  @Nonnull
  private final Directory outputDir = ThreadConfig.get(Options.JACK_FILE_OUTPUT_DIR);

  @Override
  public synchronized void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    OutputVDir vDir = new OutputDirectDir(outputDir);
    OutputVFile vFile = vDir.createOutputVFile(getFilePath(type));

    try {
      OutputStream out = new BufferedOutputStream(vFile.openWrite());
      try {
        // Write to file
        JayceWriter writer = new JayceWriter(out);
        writer.write(type, "jack " + Jack.getVersionString());
      } finally {
        out.close();
      }
    } catch (IOException e) {
      throw new JackFileException("Could not write Jack file to output '" + vFile + "'", e);
    }
  }

  @Nonnull
  protected static String getFilePath(@Nonnull JDefinedClassOrInterface type) {
    return formatter.getName(type) + JayceFileImporter.JAYCE_FILE_EXTENSION;
  }

  private static class FilePathFormatter extends BinaryQualifiedNameFormatter {

    private static final char PACKAGE_SEPARATOR = File.separatorChar;

    @Override
    protected char getPackageSeparator() {
      return PACKAGE_SEPARATOR;
    }

  }
}
