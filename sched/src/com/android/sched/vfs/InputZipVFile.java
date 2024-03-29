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

import com.android.sched.util.location.Location;
import com.android.sched.util.location.ZipLocation;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import javax.annotation.Nonnull;

class InputZipVFile extends AbstractVElement implements InputVFile {
  @Nonnull
  private final InputZipVFS vfs;
  @Nonnull
  private final ZipEntry    entry;

  InputZipVFile(@Nonnull InputZipVFS vfs, @Nonnull ZipEntry entry) {
    this.vfs   = vfs;
    this.entry = entry;
  }

  @Nonnull
  @Override
  public String getName() {
    return ZipUtils.getFileSimpleName(entry);
  }

  @Nonnull
  @Override
  public InputStream openRead() throws IOException {
    return vfs.getZipFile().getInputStream(entry);
  }

  @Override
  @Nonnull
  public Location getLocation() {
    return new ZipLocation(vfs.getLocation(), entry);
  }

  @Override
  public boolean isVDir() {
    return false;
  }

}
