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

package com.android.jack.analysis.dependency.file;

import com.android.jack.Jack;
import com.android.jack.ir.ast.JDefinedClassOrInterface;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.sched.item.Description;
import com.android.sched.item.Name;
import com.android.sched.item.Synchronized;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.schedulable.Transform;

/**
 * Collect dependencies between files and types.
 */
@Description("Collect dependencies between files and types")
@Name("FileDependenciesCollector")
@Transform(add = FileDependencies.Collected.class)
@Synchronized
public class FileDependenciesCollector implements
    RunnableSchedulable<JDefinedClassOrInterface> {

  @Override
  public synchronized void run(JDefinedClassOrInterface declaredType) throws Exception {
    if (declaredType.isExternal() || declaredType.getSourceInfo() == SourceInfo.UNKNOWN) {
      return;
    }

    Jack.getSession().getFileDependencies().addMappingBetweenJavaFileAndType(
        declaredType.getSourceInfo().getFileName(), declaredType);
  }
}
