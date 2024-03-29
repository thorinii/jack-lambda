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

package com.android.jack.cfg;

import com.android.jack.ir.ast.JStatement;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * {@link ExitBlock} represents blocks ended by return statement into the control flow graph.
 */
public class ReturnBasicBlock extends BasicBlock {

  @Nonnegative
  private static final int EXIT_BLOCK_INDEX = 0;

  @Nonnegative
  private static final int FIXED_BLOCK_COUNT = 1;

  public ReturnBasicBlock(@Nonnull ControlFlowGraph cfg, @Nonnull List<JStatement> statements) {
    super(cfg, statements, cfg.getNextBasicBlockId(), FIXED_BLOCK_COUNT);
    cfg.addNode(this);
    setSuccessor(EXIT_BLOCK_INDEX, cfg.getExitNode());
  }
}
