/*
 * Copyright (C) 2007 The Android Open Source Project
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

package com.android.jack.util;

import com.android.jack.jayce.v0002.io.ParseException;

import javax.annotation.Nonnull;

/**
 * Utility class for string operations.
 */
public class StringUtils {

  /**
   * Converts an array of UTF-8 bytes into a string.
   *
   * @param bytes {@code non-null;} the bytes to convert
   * @return {@code non-null;} the converted string
   * @throws ParseException
   */
  @Nonnull
  public static String utf8BytesToString(@Nonnull byte [] bytes) throws ParseException {
      int length = bytes.length;
      char[] chars = new char[length]; // This is sized to avoid a realloc.
      int outAt = 0;

      for (int at = 0; length > 0; /*at*/) {
          int v0 = 0x000000FF & bytes[at];
          char out;
          switch (v0 >> 4) {
              case 0x00: case 0x01: case 0x02: case 0x03:
              case 0x04: case 0x05: case 0x06: case 0x07: {
                  // 0XXXXXXX -- single-byte encoding
                  length--;
                  if (v0 == 0) {
                      // A single zero byte is illegal.
                      throw new ParseException("Invalid string value");
                  }
                  out = (char) v0;
                  at++;
                  break;
              }
              case 0x0c: case 0x0d: {
                  // 110XXXXX -- two-byte encoding
                  length -= 2;
                  if (length < 0) {
                      throw new ParseException("Invalid string value");
                  }
                  int v1 = 0x000000FF & bytes[at + 1];
                  if ((v1 & 0xc0) != 0x80) {
                      throw new ParseException("Invalid string value");
                  }
                  int value = ((v0 & 0x1f) << 6) | (v1 & 0x3f);
                  if ((value != 0) && (value < 0x80)) {
                      /*
                       * This should have been represented with
                       * one-byte encoding.
                       */
                      throw new ParseException("Invalid string value");
                  }
                  out = (char) value;
                  at += 2;
                  break;
              }
              case 0x0e: {
                  // 1110XXXX -- three-byte encoding
                  length -= 3;
                  if (length < 0) {
                      throw new ParseException("Invalid string value");
                  }
                  int v1 = 0x000000FF & bytes[at + 1];
                  if ((v1 & 0xc0) != 0x80) {
                      throw new ParseException("Invalid string value");
                  }
                  int v2 = 0x000000FF & bytes[at + 2];
                  if ((v1 & 0xc0) != 0x80) {
                      throw new ParseException("Invalid string value");
                  }
                  int value = ((v0 & 0x0f) << 12) | ((v1 & 0x3f) << 6) |
                      (v2 & 0x3f);
                  if (value < 0x800) {
                      /*
                       * This should have been represented with one- or
                       * two-byte encoding.
                       */
                      throw new ParseException("Invalid string value");
                  }
                  out = (char) value;
                  at += 3;
                  break;
              }
              default: {
                  // 10XXXXXX, 1111XXXX -- illegal
                  throw new ParseException("Invalid string value");
              }
          }
          chars[outAt] = out;
          outAt++;
      }

      return new String(chars, 0, outAt);
  }

  /**
   * Converts a string into its Java-style UTF-8 form. Java-style UTF-8
   * differs from normal UTF-8 in the handling of character '\0' and
   * surrogate pairs.
   *
   * @param string {@code non-null;} the string to convert
   * @return {@code non-null;} the UTF-8 bytes for it
   */
  @Nonnull
  public static byte[] stringToUtf8Bytes(@Nonnull String string) {
      int len = string.length();
      byte[] bytes = new byte[len * 3]; // Avoid having to reallocate.
      int outAt = 0;

      for (int i = 0; i < len; i++) {
          char c = string.charAt(i);
          if ((c != 0) && (c < 0x80)) {
              bytes[outAt] = (byte) c;
              outAt++;
          } else if (c < 0x800) {
              bytes[outAt] = (byte) (((c >> 6) & 0x1f) | 0xc0);
              bytes[outAt + 1] = (byte) ((c & 0x3f) | 0x80);
              outAt += 2;
          } else {
              bytes[outAt] = (byte) (((c >> 12) & 0x0f) | 0xe0);
              bytes[outAt + 1] = (byte) (((c >> 6) & 0x3f) | 0x80);
              bytes[outAt + 2] = (byte) ((c & 0x3f) | 0x80);
              outAt += 3;
          }
      }

      byte[] result = new byte[outAt];
      System.arraycopy(bytes, 0, result, 0, outAt);
      return result;
  }
}
