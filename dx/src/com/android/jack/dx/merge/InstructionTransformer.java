/*
 * Copyright (C) 2011 The Android Open Source Project
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

package com.android.jack.dx.merge;

import com.android.jack.dx.io.CodeReader;
import com.android.jack.dx.io.Opcodes;
import com.android.jack.dx.io.instructions.DecodedInstruction;
import com.android.jack.dx.io.instructions.ShortArrayCodeOutput;
import com.android.jack.dx.util.DexException;

final class InstructionTransformer {
  private final IndexMap indexMap;
  private final CodeReader reader;
  private DecodedInstruction[] mappedInstructions;
  private int mappedAt;

  public InstructionTransformer(IndexMap indexMap) {
    this.indexMap = indexMap;
    this.reader = new CodeReader();
    this.reader.setAllVisitors(new GenericVisitor());
    this.reader.setStringVisitor(new StringVisitor());
    this.reader.setTypeVisitor(new TypeVisitor());
    this.reader.setFieldVisitor(new FieldVisitor());
    this.reader.setMethodVisitor(new MethodVisitor());
  }

  public short[] transform(short[] encodedInstructions) throws DexException {
    DecodedInstruction[] decodedInstructions = DecodedInstruction.decodeAll(encodedInstructions);
    int size = decodedInstructions.length;

    mappedInstructions = new DecodedInstruction[size];
    mappedAt = 0;
    reader.visitAll(decodedInstructions);

    ShortArrayCodeOutput out = new ShortArrayCodeOutput(size);
    for (DecodedInstruction instruction : mappedInstructions) {
      if (instruction != null) {
        instruction.encode(out);
      }
    }

    return out.getArray();
  }

  private class GenericVisitor implements CodeReader.Visitor {
    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction one) {
      mappedInstructions[mappedAt++] = one;
    }
  }

  private class StringVisitor implements CodeReader.Visitor {
    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction one) {
      int stringId = one.getIndex();
      int mappedId = indexMap.adjustString(stringId);
      boolean isJumbo = (one.getOpcode() == Opcodes.CONST_STRING_JUMBO);
      jumboCheck(isJumbo, mappedId);
      mappedInstructions[mappedAt++] = one.withIndex(mappedId);
    }
  }

  private class FieldVisitor implements CodeReader.Visitor {
    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction one) {
      int fieldId = one.getIndex();
      int mappedId = indexMap.adjustField(fieldId);
      boolean isJumbo = (one.getOpcode() == Opcodes.CONST_STRING_JUMBO);
      jumboCheck(isJumbo, mappedId);
      mappedInstructions[mappedAt++] = one.withIndex(mappedId);
    }
  }

  private class TypeVisitor implements CodeReader.Visitor {
    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction one) {
      int typeId = one.getIndex();
      int mappedId = indexMap.adjustType(typeId);
      boolean isJumbo = (one.getOpcode() == Opcodes.CONST_STRING_JUMBO);
      jumboCheck(isJumbo, mappedId);
      mappedInstructions[mappedAt++] = one.withIndex(mappedId);
    }
  }

  private class MethodVisitor implements CodeReader.Visitor {
    @Override
    public void visit(DecodedInstruction[] all, DecodedInstruction one) {
      int methodId = one.getIndex();
      int mappedId = indexMap.adjustMethod(methodId);
      boolean isJumbo = (one.getOpcode() == Opcodes.CONST_STRING_JUMBO);
      jumboCheck(isJumbo, mappedId);
      mappedInstructions[mappedAt++] = one.withIndex(mappedId);
    }
  }

  private static void jumboCheck(boolean isJumbo, int newIndex) {
    if (!isJumbo && (newIndex > 0xffff)) {
      throw new DexException(
          "Cannot merge new index " + newIndex + " into a non-jumbo instruction!");
    }
  }
}
