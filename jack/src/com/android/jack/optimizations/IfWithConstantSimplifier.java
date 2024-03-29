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

package com.android.jack.optimizations;

import com.android.jack.Options;
import com.android.jack.analysis.DefinitionMarker;
import com.android.jack.analysis.UseDefsMarker;
import com.android.jack.cfg.BasicBlock;
import com.android.jack.cfg.BasicBlockMarker;
import com.android.jack.cfg.ConditionalBasicBlock;
import com.android.jack.cfg.ControlFlowGraph;
import com.android.jack.ir.ast.JBlock;
import com.android.jack.ir.ast.JBooleanLiteral;
import com.android.jack.ir.ast.JGoto;
import com.android.jack.ir.ast.JIfStatement;
import com.android.jack.ir.ast.JLabel;
import com.android.jack.ir.ast.JLabeledStatement;
import com.android.jack.ir.ast.JMethod;
import com.android.jack.ir.ast.JNode;
import com.android.jack.ir.ast.JStatement;
import com.android.jack.ir.ast.JSwitchStatement;
import com.android.jack.ir.ast.JVariableRef;
import com.android.jack.ir.ast.JVisitor;
import com.android.jack.ir.sourceinfo.SourceInfo;
import com.android.jack.transformations.ast.NoImplicitBlock;
import com.android.jack.transformations.request.AppendBefore;
import com.android.jack.transformations.request.AppendStatement;
import com.android.jack.transformations.request.PrependAfter;
import com.android.jack.transformations.request.PrependStatement;
import com.android.jack.transformations.request.Remove;
import com.android.jack.transformations.request.Replace;
import com.android.jack.transformations.request.TransformationRequest;
import com.android.jack.transformations.threeaddresscode.ThreeAddressCodeForm;
import com.android.jack.util.filter.Filter;
import com.android.sched.item.Description;
import com.android.sched.schedulable.Constraint;
import com.android.sched.schedulable.RunnableSchedulable;
import com.android.sched.util.config.ThreadConfig;

import java.util.List;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Simplify if that uses only boolean constants.
 */
@Description("Simplify if that uses only boolean constants.")
@Constraint(need = {UseDefsMarker.class, NoImplicitBlock.class, ThreeAddressCodeForm.class,
    ControlFlowGraph.class})
public class IfWithConstantSimplifier implements RunnableSchedulable<JMethod> {

  @Nonnull
  private final Filter<JMethod> filter = ThreadConfig.get(Options.METHOD_FILTER);

  @Nonnegative
  private static int count;

  private static class Visitor extends JVisitor {

    @Nonnull
    private final JMethod method;

    public Visitor(@Nonnull JMethod method) {
      this.method = method;
    }

    @Override
    public boolean visit(@Nonnull JIfStatement ifStmt) {

      if (ifStmt.getIfExpr() instanceof JVariableRef) {
        /*
         * Limit optimization to synthetic variables in order to optimize code generated by Jack
         * (means && and ||). This optimization replace variable assignment by goto, to be safe, it
         * is required to check that paths starting on the target of the new generated path and
         * reaching the ifStmt must again contains definition of the removed variable to avoid
         * runtime verifier error due to a used variable without definition. This is not yet done.
         */
        if (((JVariableRef) ifStmt.getIfExpr()).getTarget().isSynthetic()) {
          UseDefsMarker udm = ifStmt.getIfExpr().getMarker(UseDefsMarker.class);
          assert udm != null;
          assert !udm.isWithoutDefinition();

          boolean allDefsAreBooleanCstAndUseByIfStmt = true;
          JLabeledStatement thenLabel = null;
          JLabeledStatement elseLabel = null;
          TransformationRequest tr = new TransformationRequest(method);
          SourceInfo si = ifStmt.getThenStmt().getSourceInfo();

          JStatement elseStmt = ifStmt.getElseStmt();

          for (DefinitionMarker dm : udm.getDefs()) {
            if (dm.hasValue() && dm.getValue() instanceof JBooleanLiteral
                && dm.isUsedOnlyOnce() && !hasCodeBetweenDefAndUsage(dm, ifStmt)) {
                if (((JBooleanLiteral) dm.getValue()).getValue() == true) {
                  // Branch to then block

                  if (thenLabel == null) {
                    thenLabel = new JLabeledStatement(
                        si, new JLabel(si, "ifSimplierThen_" + count), new JBlock(si));
                    tr.append(new PrependStatement((JBlock) ifStmt.getThenStmt(), thenLabel));
                  }

                  assert thenLabel != null;
                  tr.append(new Replace(dm.getDefinition().getParent(), new JGoto(si, thenLabel)));
                } else {
                  // Branch to else block
                  if (elseStmt != null) {
                    if (elseLabel == null) {
                      elseLabel = new JLabeledStatement(
                          si, new JLabel(si, "ifSimplierElse_" + count), new JBlock(si));
                      tr.append(new PrependStatement((JBlock) elseStmt, elseLabel));
                    }
                  } else {
                    if (elseLabel == null) {
                      elseLabel = new JLabeledStatement(
                          si, new JLabel(si, "ifSimplierEnd_" + count), new JBlock(si));

                      tr.append(new PrependAfter(ifStmt, elseLabel));
                    }
                  }

                  tr.append(new Replace(dm.getDefinition().getParent(), new JGoto(si, elseLabel)));
                }
            } else {
              allDefsAreBooleanCstAndUseByIfStmt = false;
            }

          }

          if (allDefsAreBooleanCstAndUseByIfStmt) {
            // If statement will be removed
            tr.append(new AppendBefore(ifStmt, ifStmt.getThenStmt()));

            if (elseStmt != null) {
              tr.append(new AppendBefore(ifStmt, elseStmt));

              // Add goto to the end of then block to skip else block.
              JBlock thenBb = (JBlock) ifStmt.getThenStmt();
              List<JStatement> thenStatements = thenBb.getStatements();

              JLabeledStatement endLabel = new JLabeledStatement(
                  si, new JLabel(si, "ifSimplierEnd_" + count), new JBlock(si));

              if (!thenStatements.isEmpty()) {
                JStatement lastStatement = getLastStatement(thenStatements);
                tr.append(new PrependAfter(lastStatement, new JGoto(si, endLabel)));
              } else {
                tr.append(new AppendStatement(thenBb, new JGoto(si, endLabel)));
              }

              tr.append(new PrependAfter(ifStmt, endLabel));
            }
            tr.append(new Remove(ifStmt));
          }

          count++;
          tr.commit();
        }
      }

      return false;
    }

    @Override
    public boolean visit(@Nonnull JSwitchStatement switchStmt) {
      super.visit(switchStmt);
      this.accept(switchStmt.getExpr());
      return false;
    }

    @Nonnull
    private JStatement getLastStatement(@Nonnull List<JStatement> thenStatements) {
      JStatement lastStatement = thenStatements.get(thenStatements.size() - 1);
      while (lastStatement instanceof JBlock) {
        thenStatements = ((JBlock) lastStatement).getStatements();
        lastStatement = thenStatements.get(thenStatements.size() - 1);
      }
      return lastStatement;
    }

    private boolean hasCodeBetweenDefAndUsage(
        @Nonnull DefinitionMarker dm, @Nonnull JIfStatement ifStmt) {
      BasicBlockMarker ifStmtBbMarker = ifStmt.getMarker(BasicBlockMarker.class);
      assert ifStmtBbMarker != null;
      BasicBlock ifStmtBasicBlock = ifStmtBbMarker.getBasicBlock();


      JNode defStmt = dm.getDefinition().getParent();
      BasicBlockMarker bbm = defStmt.getMarker(BasicBlockMarker.class);
      assert bbm != null;

      BasicBlock defBasicBlock = bbm.getBasicBlock();
      List<JStatement> statementsOfDefBlock = defBasicBlock.getStatements();
      int lastStmtIndex = statementsOfDefBlock.size() - 1;

      if (defBasicBlock == ifStmtBasicBlock) {
        // Definition and instruction 'if' using the definition belongs to the same block.
        assert statementsOfDefBlock.get(lastStmtIndex) == ifStmt;
        if (statementsOfDefBlock.get(lastStmtIndex - 1) == defStmt) {
          // There is no code between definition and usage into 'if' instruction if the definition
          // is the previous instruction of 'if'.
          return false;
        }
      } else {

        // The definition must be the last statement of the definition block, otherwise there is
        // automatically code between the definition and the usage since they does not belong to
        // the same block.
        if (statementsOfDefBlock.get(lastStmtIndex) == defStmt) {
          // The instruction 'if' using the definition must be the first instruction of successors
          // of the definition block otherwise it means that there is code between us.
          for (BasicBlock succ : defBasicBlock.getSuccessors()) {
            if (succ.getStatements().get(0) == ifStmt) {
              return false;
            }
          }
        }
      }

      return true;
    }
  }

  @Override
  public void run(@Nonnull JMethod method) throws Exception {
    if (method.getEnclosingType().isExternal() || method.isNative() || method.isAbstract()
        || !filter.accept(this.getClass(), method)) {
      return;
    }

    ControlFlowGraph cfg = method.getMarker(ControlFlowGraph.class);
    assert cfg != null;

    for (BasicBlock bb : cfg.getNodes()) {
      if (bb instanceof ConditionalBasicBlock) {
        for (JStatement stmt : bb.getStatements()) {
          Visitor visitor = new Visitor(method);
          visitor.accept(stmt);
        }
      }
    }
  }
}
