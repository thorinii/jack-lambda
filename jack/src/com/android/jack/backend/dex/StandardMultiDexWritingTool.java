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

import com.android.jack.JackIOException;
import com.android.jack.JackUserException;
import com.android.jack.tools.merger.JackMerger;
import com.android.jack.tools.merger.MergeOverflow;
import com.android.sched.util.codec.ImplementationName;
import com.android.sched.vfs.InputVFile;
import com.android.sched.vfs.OutputVDir;
import com.android.sched.vfs.OutputVFile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * A {@link DexWritingTool} that merges dex files, each one corresponding to a type, in several dex
 * files.
 */
@ImplementationName(iface = DexWritingTool.class, name = "multidex",
    description = "allow emitting several dex files")
public class StandardMultiDexWritingTool extends DexWritingTool {

  @Override
  public void write(@Nonnull OutputVDir outputVDir) throws JackIOException {
    int dexCount = 1;
    JackMerger merger = new JackMerger(createDexFile());
    OutputVFile outputDex = getOutputDex(outputVDir, dexCount++);
    List<InputVFile> mainDexList = new ArrayList<InputVFile>();
    List<InputVFile> anyDexList = new ArrayList<InputVFile>();
    fillDexLists(mainDexList, anyDexList);

    for (InputVFile currentDex : mainDexList) {
      try {
        mergeDex(merger, currentDex);
      } catch (MergeOverflow e) {
        throw new JackUserException(
            "Too many classes in main dex. Index overflow while merging dex files", e);
      }
    }

    for (InputVFile currentDex : anyDexList) {
      try {
        mergeDex(merger, currentDex);
      } catch (MergeOverflow e) {
        finishMerge(merger, outputDex);
        outputDex = getOutputDex(outputVDir, dexCount++);
        merger = new JackMerger(createDexFile());
        try {
          mergeDex(merger, currentDex);
        } catch (MergeOverflow e1) {
          // This should not happen, the type is not too big, we've just read it from a dex.
          throw new AssertionError();
        }
      }
    }

    finishMerge(merger, outputDex);
  }
}