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

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * This {@link StringCodec} is used to create an instance of {@link Long}
 */
public class LongCodec implements StringCodec<Long>{
  private long min;
  private long max;

  public LongCodec() {
    this(Long.MIN_VALUE, Long.MAX_VALUE);
  }

  public LongCodec(long min, long max) {
    this.min = min;
    this.max = max;
  }

  public void setMin(long min) {
    this.min = min;
  }

  public void setMax(long max) {
    this.max = min;
  }

  @Override
  @Nonnull
  public String getUsage() {
    return "an integer belonging to [" + min + " .. " + max + "]";
  }

  @Override
  @Nonnull
  public List<ValueDescription> getValueDescriptions() {
    return Collections.<ValueDescription> emptyList();
  }

  @Override
  @Nonnull
  public Long checkString(@Nonnull CodecContext context, @Nonnull String string)
      throws ParsingException {
    try {
      Long l = Long.valueOf(string);
      try {
        checkValue(context, l);
      } catch (CheckingException e) {
        throw new ParsingException(e);
      }

      return l;
    } catch (NumberFormatException e) {
      throw new ParsingException(
          "The value must be " + getUsage() + " but is '" + string + "'");
    }
  }

  @Override
  public void checkValue(@Nonnull CodecContext context, @Nonnull Long l)
      throws CheckingException {
    long v = l.longValue();

    if (v < min || v > max) {
      throw new CheckingException(
          "The value must be " + getUsage() + " but is " + l);
    }
  }

  @Override
  @Nonnull
  public Long parseString(@Nonnull CodecContext context, @Nonnull String string) {
    try {
      return checkString(context, string);
    } catch (ParsingException e) {
      throw new ConfigurationError(e);
    }
  }

  @Override
  @Nonnull
  public String formatValue(@Nonnull Long l) {
    return l.toString();
  }
}