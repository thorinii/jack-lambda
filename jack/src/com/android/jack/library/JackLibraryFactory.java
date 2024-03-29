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

import com.android.jack.library.v0001.OutputJackLibraryImpl;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.vfs.InputOutputVFS;
import com.android.sched.vfs.InputVFS;
import com.android.sched.vfs.InputVFile;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Factory to instantiate {@link JackLibrary}.
 */
public abstract class JackLibraryFactory {

  @Nonnull
  private static Logger logger = LoggerFactory.getLogger();

  public static final int DEFAULT_MAJOR_VERSION = 1;

  @Nonnull
  private static final String VERSION_FORMAT = "%04d";

  @Nonnull
  public static String getVersionString(@Nonnegative int version) {
    return String.format(VERSION_FORMAT, Integer.valueOf(version));
  }

  @Nonnull
  public static InputJackLibrary getInputLibrary(@Nonnull InputVFS vdir)
      throws LibraryVersionException, LibraryFormatException, NotJackLibraryException {
    Properties libraryProperties = loadLibraryProperties(vdir);
    String majorVersion = getMajorVersionAsString(vdir, libraryProperties);

    InputJackLibrary inputJackLibrary = (InputJackLibrary) instantiateConstructorWithParameters(
        vdir, "com.android.jack.library.v" + majorVersion + ".InputJackLibraryImpl",
        new Class[] {InputVFS.class, Properties.class}, new Object[] {vdir, libraryProperties},
        String.valueOf(majorVersion));

    return inputJackLibrary;
  }

  @Nonnull
  public static OutputJackLibrary getOutputLibrary(@Nonnull InputOutputVFS vdir,
      @Nonnull String emitterId, @Nonnull String emitterVersion) {
    return new OutputJackLibraryImpl(vdir, emitterId, emitterVersion);
  }

  private static String getMajorVersionAsString(@Nonnull InputVFS vdir,
      @Nonnull Properties libraryProperties) throws LibraryFormatException {
    try {
      return (getVersionString(
          Integer.parseInt((String) libraryProperties.get(JackLibrary.KEY_LIB_MAJOR_VERSION))));
    } catch (NumberFormatException e) {
      logger.log(Level.SEVERE, "Fails to parse the property " + JackLibrary.KEY_LIB_MAJOR_VERSION
          + " from the library " + vdir, e);
      throw new LibraryFormatException(vdir.getLocation());
    }
  }

  @Nonnull
  private static Properties loadLibraryProperties(@Nonnull InputVFS vfs)
      throws NotJackLibraryException {
    Properties libraryProperties = new Properties();

    try {
      InputVFile libProp =
          vfs.getRootInputVDir().getInputVFile(JackLibrary.LIBRARY_PROPERTIES_VPATH);
      libraryProperties.load(libProp.openRead());
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Fails to read "
          + JackLibrary.LIBRARY_PROPERTIES_VPATH.getPathAsString('/') + " from "
          + vfs, e);
      throw new NotJackLibraryException(vfs.getLocation());
    }

    return libraryProperties;
  }

  @Nonnull
  private static Object instantiateConstructorWithParameters(@Nonnull InputVFS vdir,
      @Nonnull String className, @Nonnull Class<?>[] parameterTypes,
      @Nonnull Object[] parameterInstances, @Nonnull String version)
      throws LibraryVersionException, LibraryFormatException {
    Object constructorInstance = null;
    try {
      Class<?> libraryReaderClass = Class.forName(className);
      Constructor<?> constructor = libraryReaderClass.getConstructor(parameterTypes);
      constructorInstance = constructor.newInstance(parameterInstances);
    } catch (SecurityException e) {
      throw new AssertionError();
    } catch (IllegalArgumentException e) {
      throw new AssertionError("Illegal argument for library constructor for version " + version);
    } catch (ClassNotFoundException e) {
      throw new LibraryVersionException(
          "Library " + vdir + " has an unsupported version " + version);
    } catch (NoSuchMethodException e) {
      throw new AssertionError("Library constructor not found for version " + version);
    } catch (InstantiationException e) {
      throw new AssertionError("Problem instantiating a library for version " + version);
    } catch (IllegalAccessException e) {
      throw new AssertionError("Problem accessing library constructor for version " + version);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof LibraryFormatException) {
        throw ((LibraryFormatException) cause);
      } else if (cause instanceof LibraryVersionException) {
        throw ((LibraryVersionException) cause);
      } else if (cause instanceof RuntimeException) {
        throw ((RuntimeException) cause);
      } else if (cause instanceof Error) {
        throw ((Error) cause);
      }
      throw new AssertionError(cause);
    }
    return constructorInstance;
  }
}
