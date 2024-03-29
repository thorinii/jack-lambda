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

package com.android.sched.scheduler;

import com.android.sched.item.Component;
import com.android.sched.schedulable.AdapterSchedulable;
import com.android.sched.schedulable.ProcessorSchedulable;

import javax.annotation.Nonnull;

/**
 * A structure that allows to manually build a {@link Plan} that can be a subplan of another plan.
 * @param <T> the root <i>data</i> type of the {@code Plan}
 */
public class SubPlanBuilder<T extends Component> {
  @Nonnull
  private final SchedulableManager schedulableManager = SchedulableManager.getSchedulableManager();

  @Nonnull
  private final Class<T> runOn;
  @Nonnull
  protected Plan<T> plan;

  protected SubPlanBuilder(Class<T> runOn) {
    this.runOn = runOn;

    plan = new Plan<T>();
  }

  /**
   * Adds a {@link ProcessorSchedulable} at the end of this {@code Plan}.
   * <p>
   * The <i>data</i> type of the {@code RunnableSchedulable} must be compatible with this
   * {@code Plan}.
   */
  public void append(@Nonnull Class<? extends ProcessorSchedulable<T>> runner) {
    ManagedRunnable ir = (ManagedRunnable) (schedulableManager.getManagedSchedulable(runner));
    if (ir == null) {
      throw new SchedulableNotRegisteredError(runner);
    }

    if (!ir.getRunOn().equals(runOn)) {
      throw new PlanError("'" + ir.getName() + "' expect to be applied on '" + ir.getRunOn()
          + "' but was on '" + runOn + "'");
    }

    append(ir);
  }

  public void append(@Nonnull ManagedRunnable runner) {
    assert runner != null;
    assert runner.getRunOn().equals(runOn) : "Expect '" + runner.getRunOn() + "', have '" + runOn
        + "'";

    plan.appendStep(new PlanStep(runner));
  }

  /**
   * Adds a {@link AdapterSchedulable} at the end of this {@code Plan} thus creating a
   * subplan which <i>data</i> type is the output <i>data</i> type of the {@code VisitorSchedulable}
   * .
   * <p>
   * The input <i>data</i> type of the {@code VisitorSchedulable} must be compatible with this
   * {@code Plan}.
   *
   * @return the new subplan
   */
  @Nonnull
  public <U extends Component> SubPlanBuilder<U> appendSubPlan(
      @Nonnull Class<? extends AdapterSchedulable<T, U>> visitor) {
    ManagedVisitor ia = (ManagedVisitor) (schedulableManager.getManagedSchedulable(visitor));
    if (ia == null) {
      throw new SchedulableNotRegisteredError(visitor);
    }

    if (!ia.getRunOn().equals(runOn)) {
      throw new PlanError("'" + ia.getName() + "' expect to be applied on '" + ia.getRunOn()
          + "' but was on '" + runOn + "'");
    }

    return appendSubPlan(ia);
  }

  @SuppressWarnings("unchecked")
  @Nonnull
  public <U extends Component> SubPlanBuilder<U> appendSubPlan(@Nonnull ManagedVisitor visitor) {
    assert visitor != null;
    assert visitor.getRunOn().equals(runOn) : "Expect '" + visitor.getRunOn() + "', have '" + runOn
        + "'";

    SubPlanBuilder<U> subPlanBuilder = new SubPlanBuilder<U>((Class<U>) visitor.getRunOnAfter());
    plan.appendStep(new PlanStep(visitor, subPlanBuilder.plan));

    return subPlanBuilder;
  }

  /**
   * Returns the root <i>data</i> type of this {@code Plan}.
   */
  @Nonnull
  public Class<T> getRunOn() {
    return runOn;
  }

  @Nonnull
  @Override
  public String toString() {
    return plan.toString();
  }

  @Nonnull
  public String getDescription() {
    return plan.getDescription();
  }

  @Nonnull
  public String getDetailedDescription() {
    return plan.getDetailedDescription();
  }
}
