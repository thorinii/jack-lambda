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

package com.android.jack.backend.dex;

import com.android.jack.Jack;
import com.android.jack.JackIOException;
import com.android.jack.backend.dex.rop.CodeItemBuilder;
import com.android.jack.dx.dex.DexOptions;
import com.android.jack.dx.dex.file.DexFile;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.formatter.BinaryQualifiedNameFormatter;
import com.android.jack.library.FileType;
import com.android.jack.library.FileTypeDoesNotExistException;
import com.android.jack.library.InputLibrary;
import com.android.jack.library.OutputLibrary;
import com.android.jack.library.TypeInInputLibraryLocation;
import com.android.jack.scheduling.marker.ClassDefItemMarker;
import com.android.jack.scheduling.marker.DexCodeMarker;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.Produce;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;
import com.android.sched.util.location.Location;
import com.android.sched.util.stream.ByteStreamSucker;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVFile;
import com.android.sched.vfs.VPath;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.annotation.Nonnull;

/**
 * Write intermediate dex files per type.
 */
@Description("Write intermediate dex files per type")
@Constraint(need = {DexCodeMarker.class, ClassDefItemMarker.Complete.class})
@Produce(IntermediateDexProduct.class)
public class IntermediateDexPerTypeWriter extends DexWriter implements
    RunnableSchedulable<JDefinedClassOrInterface> {

  @Nonnull
  private final OutputLibrary outputLibrary = Jack.getSession().getJackInternalOutputLibrary();

  private final boolean forceJumbo = ThreadConfig.get(CodeItemBuilder.FORCE_JUMBO).booleanValue();

  @Override
  public void run(@Nonnull JDefinedClassOrInterface type) throws Exception {
    OutputVFile vFile;

    Location loc = type.getLocation();
    if (loc instanceof TypeInInputLibraryLocation) {
      InputVFile in;
      InputLibrary inputLibrary =
          ((TypeInInputLibraryLocation) loc).getInputLibraryLocation().getInputLibrary();
      if (inputLibrary.containsFileType(FileType.DEX)) {
        try {
          in = inputLibrary.getFile(FileType.DEX,
              new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
        } catch (FileTypeDoesNotExistException e) {
          // this was created by Jack, so this should not happen
          throw new AssertionError(e);
        }

        vFile = outputLibrary.createFile(FileType.DEX,
            new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));

        InputStream is = in.openRead();
        OutputStream os = vFile.openWrite();
        try {
          new ByteStreamSucker(is, os, true).suck();
        } finally {
          is.close(); // is != null or check before
        }

        return;
      }
    }

    ClassDefItemMarker cdiMarker = type.getMarker(ClassDefItemMarker.class);
    assert cdiMarker != null;

    DexOptions options = new DexOptions();
    options.forceJumbo = forceJumbo;
    DexFile typeDex = new DexFile(options);
    typeDex.add(cdiMarker.getClassDefItem());
    OutputStream outStream = null;
    try {
      vFile = outputLibrary.createFile(FileType.DEX,
          new VPath(BinaryQualifiedNameFormatter.getFormatter().getName(type), '/'));
    } catch (IOException e) {
      throw new JackIOException("Could not create Dex file in output "
          + outputLibrary.getLocation().getDescription() + " for type "
          + Jack.getUserFriendlyFormatter().getName(type), e);
    }
    try {
      outStream = vFile.openWrite();
      typeDex.prepare();
      typeDex.writeTo(outStream, null, false);
    } catch (IOException e) {
      throw new JackIOException("Could not write Dex file to output " + vFile, e);
    } finally {
      if (outStream != null) {
        outStream.close();
      }
    }
  }
}
