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

package com.android.sched.util.log.tracer.probe;

import com.google.monitoring.runtime.instrumentation.AllocationRecorder;
import com.google.monitoring.runtime.instrumentation.Sampler;

import com.android.sched.util.config.ConfigurationError;
import com.android.sched.util.log.LoggerFactory;
import com.android.sched.util.log.Tracer;
import com.android.sched.util.log.TracerFactory;

import java.util.logging.Level;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Probe which count the heap memory usage.
 */
public abstract class HeapAllocationProbe extends MemoryBytesProbe {
  protected HeapAllocationProbe(@Nonnull String description) {
    super(description, MIN_PRIORITY);
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  /**
   * Protected class to access allocation counters and enable state.
   */
  public static class ThreadLocalCounting {
    @Nonnegative
    public long   count;
    @Nonnegative
    public long   size;
  }

  @Nonnull
  protected static final ThreadLocal<ThreadLocalCounting> alloc =
      new ThreadLocal<ThreadLocalCounting>(){
    @Override
    protected ThreadLocalCounting initialValue() {
      return new ThreadLocalCounting();
    }
  };

  private static boolean installed = false;

  public static void ensureInstall() {
    if (!installed) {
      installed = true;
      try {
        // test if allocation.jar is in the class path
        Class.forName("com.google.monitoring.runtime.instrumentation.Sampler");
        // If yes, install the sampler
        Instrumentation.install();
      } catch (ClassNotFoundException e) {
        // If not, warn and forget
        LoggerFactory.getLogger().log(Level.WARNING,
          "Allocation instrumenter agent is not specified on the JVM command line"
            + " (see -javaagent)");
      }
    }
  }

  private static class Instrumentation {
    private static void install() {
      Sampler sampler = new Sampler() {
        @Override
        public void sampleAllocation(
            int count, String desc, Object newObj, long size) {
          try {
            Tracer tracer = TracerFactory.getTracer();

            if (tracer.isTracing()) {
              ThreadLocalCounting tlc = alloc.get();
              tlc.count++;
              tlc.size += size;

              tracer.registerObject(newObj, size, count);
            }
          } catch (ConfigurationError e) {
            // Do not collect for thread without config (No Tracer available)
          }
        }
      };

      AllocationRecorder.addSampler(sampler);
    }
  }
}
