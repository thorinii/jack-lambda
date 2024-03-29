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

package com.android.sched.vfs;

import java.io.File;
import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * Thrown when listing a directory content failed.
 */
public class ListDirException extends IOException {

  private static final long serialVersionUID = 1L;
  @Nonnull
  private final File dir;

  public ListDirException(@Nonnull File dir) {
    this.dir = dir;
  }

  @Override
  public String getMessage() {
    return "Failed to list directory content '" + dir.getPath() + "'";
  }
}
