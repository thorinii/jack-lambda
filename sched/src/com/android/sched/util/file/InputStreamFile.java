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

package com.android.sched.util.file;

import com.android.sched.util.ConcurrentIOException;
import com.android.sched.util.stream.UncloseableInputStream;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.annotation.Nonnull;

/**
 * Class representing a input stream from a file path or a standard input.
 */
public class InputStreamFile extends StreamFile {
  public InputStreamFile(@Nonnull String name,
      @Nonnull ChangePermission change)
      throws FileAlreadyExistsException,
      CannotCreateFileException,
      CannotSetPermissionException,
      WrongPermissionException,
      NoSuchFileException,
      NotFileOrDirectoryException {
    super(name, null /* hooks */, Existence.MUST_EXIST, Permission.READ, change);
  }

  public InputStreamFile() {
    super(Permission.READ);
  }

  @Nonnull
  public InputStream getInputStream() {
    if (file == null) {
      return new UncloseableInputStream(System.in);
    } else {
      clearRemover();
      try {
        return new FileInputStream(file);
      } catch (FileNotFoundException e) {
        throw new ConcurrentIOException(e);
      }
    }
  }
}