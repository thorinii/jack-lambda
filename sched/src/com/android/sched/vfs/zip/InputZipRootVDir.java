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

package com.android.sched.vfs.zip;

import com.google.common.base.Splitter;

import com.android.sched.vfs.InputRootVDir;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.VPath;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.annotation.Nonnull;

/**
 * Virtual directory for viewing the content of a zip file.
 */
public class InputZipRootVDir extends InputZipVDir implements Closeable, InputRootVDir {

  @Nonnull
  public static final char IN_ZIP_SEPARATOR = '/';

  @Nonnull
  private final ZipFile zip;

  public InputZipRootVDir(@Nonnull File zipFile) throws IOException {
    super("", zipFile, new ZipEntry(""));

    zip = new ZipFile(zipFile);
    Splitter splitter = Splitter.on(IN_ZIP_SEPARATOR);

    for (Enumeration<? extends ZipEntry> entries = zip.entries(); entries.hasMoreElements();) {
      ZipEntry entry = entries.nextElement();
      if (!entry.isDirectory()) {
        String entryName = entry.getName();
        Iterator<String> names = splitter.split(entryName).iterator();
        @SuppressWarnings("resource")
        InputZipVDir dir = this;
        StringBuilder inZipPath = new StringBuilder();
        String simpleName = null;
        while (names.hasNext()) {
          simpleName = names.next();
          if (names.hasNext()) {
            inZipPath.append(IN_ZIP_SEPARATOR).append(simpleName);
            InputZipVDir nextDir = (InputZipVDir) dir.subs.get(simpleName);
            if (nextDir == null) {
              nextDir = new InputZipVDir(simpleName, zipFile, new ZipEntry(inZipPath.toString()));
              dir.subs.put(simpleName, nextDir);
            }
            dir = nextDir;
          }
        }
        dir.subs.put(simpleName, new InputZipVFile(simpleName, zip, entry));
      }
    }
  }

  @Override
  public void close() throws IOException {
    zip.close();
  }

  @Override
  @Nonnull
  public InputVFile getInputVFile(@Nonnull VPath path) {
    ZipEntry entry = zip.getEntry(path.getPathAsString(IN_ZIP_SEPARATOR));
    return new InputZipVFile(path.getLastPathElement(), zip, entry);
  }
}