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

import com.android.sched.util.location.FileOrDirLocation;

import java.io.IOException;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

/**
 * Exception when a file or directory can not be created.
 */
public class CannotCreateFileException extends IOException {
  private static final long serialVersionUID = 1L;

  public CannotCreateFileException(@Nonnull FileOrDirLocation location) {
    this(location, null);
  }

  public CannotCreateFileException(@Nonnull FileOrDirLocation location,
      @CheckForNull Throwable cause) {
    super(location.getDescription() + " can not be created", cause);
  }
}
