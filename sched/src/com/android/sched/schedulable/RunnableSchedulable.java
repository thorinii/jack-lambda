/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.android.sched.schedulable;

import com.android.sched.item.Component;

import javax.annotation.Nonnull;

/**
 * A {@code Schedulable} which performs a process on an instance of type T.
 *
 * @param <T> the type of the instance to process
 */
public interface RunnableSchedulable<T extends Component> extends ProcessorSchedulable<T> {

  /**
   * Must implement the processing of the instance of type T.
   *
   * @throws Exception if any Exception is thrown during the process
   */
  public void run(@Nonnull T t) throws Exception;
}
