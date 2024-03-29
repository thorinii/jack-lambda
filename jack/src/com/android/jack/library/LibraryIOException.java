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

package com.android.jack.library;

import com.android.jack.LibraryException;
import com.android.sched.util.location.Location;

import javax.annotation.Nonnull;

/**
 * Exception representing a problem related to an io during library access.
 */
public class LibraryIOException extends LibraryException {

  private static final long serialVersionUID = 1L;

  @Nonnull
  private final Location location;

  public LibraryIOException(@Nonnull Location location, @Nonnull Throwable cause) {
    super(cause);
    this.location = location;
  }

  @Override
  @Nonnull
  public String getMessage() {
    return location.getDescription() + " is an invalid library: " + getCause().getMessage();
  }
}
