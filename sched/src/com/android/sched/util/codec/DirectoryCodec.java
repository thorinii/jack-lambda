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

package com.android.sched.util.codec;


import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.file.Directory;
import com.android.sched.util.file.FileOrDirectory.ChangePermission;
import com.android.sched.util.file.FileOrDirectory.Existence;
import com.android.sched.util.file.FileOrDirectory.Permission;

import java.io.IOException;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link Directory}.
 */
public class DirectoryCodec extends FileOrDirCodec
    implements StringCodec<Directory> {

  public DirectoryCodec(@Nonnull Existence existence, int permissions) {
    super(existence, permissions);

    assert (permissions & Permission.EXECUTE)
        == 0 : "Execute permission is not supported by " + DirectoryCodec.class.getSimpleName();
  }

  @Nonnull
  public DirectoryCodec changeOwnerPermission() {
    setChangePermission(ChangePermission.OWNER);

    return this;
  }

  @Nonnull
  public DirectoryCodec changeAllPermission() {
    setChangePermission(ChangePermission.EVERYBODY);

    return this;
  }

  @Override
  @Nonnull
  public String getUsage() {
    StringBuilderAppender sb = new StringBuilderAppender(", ");

    sb.append("a path to a directory");
    sb.append(" (must ");

    sb.append(existence == Existence.MUST_EXIST, "exist");
    sb.append(existence == Existence.NOT_EXIST,  "not exist");

    sb.append((permissions & Permission.READ)     != 0, "be readable");
    sb.append((permissions & Permission.WRITE)    != 0, "be writable");

    sb.append(")");

    return sb.toString();
  }

  @Override
  @Nonnull
  public Directory checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    try {
      return new Directory(string, context.getRunnableHooks(), existence, permissions, change);
    } catch (IOException e) {
      throw new ParsingException(e.getMessage(), e);
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull Directory dir) {
  }

  @Override
  @Nonnull
  public Directory parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Directory directory) {
    return directory.getPath();
  }
}
