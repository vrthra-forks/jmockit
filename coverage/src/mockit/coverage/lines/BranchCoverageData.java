/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.lines;

import mockit.coverage.*;
import mockit.external.asm4.*;

/**
 * Coverage data gathered for a branch inside a line of source code.
 */
public final class BranchCoverageData extends LineSegmentData
{
   private static final long serialVersionUID = 1003335601845442606L;

   // Static data:
   public final transient Label jumpSource;
   public final transient Label jumpTarget;

   // Runtime data (and static if any execution count is -1, meaning lack of the jump target):
   private int jumpExecutionCount;

   BranchCoverageData(Label jumpSource, Label jumpTarget)
   {
      this.jumpSource = jumpSource;
      this.jumpTarget = jumpTarget;
      jumpExecutionCount = -1;
      executionCount = -1;
   }

   public void setHasJumpTarget() { jumpExecutionCount = 0; }
   public void setHasNoJumpTarget() { executionCount = 0; }

   void registerJumpExecution(CallPoint callPoint)
   {
      assert jumpExecutionCount >= 0 : "Illegal registerJumpExecution";
      jumpExecutionCount++;
      addCallPointIfAny(callPoint);
   }

   void registerNoJumpExecution(CallPoint callPoint)
   {
      assert executionCount >= 0 : "Illegal registerNoJumpExecution";
      executionCount++;
      addCallPointIfAny(callPoint);
   }

   @Override
   public boolean isCovered()
   {
      return super.isCovered() || jumpExecutionCount > 0;
   }

   @Override
   public int getExecutionCount()
   {
      return executionCount > 0 ? executionCount : jumpExecutionCount > 0 ? jumpExecutionCount : 0;
   }

   void addCountsFromPreviousTestRun(BranchCoverageData previousData)
   {
      addExecutionCountAndCallPointsFromPreviousTestRun(previousData);
      jumpExecutionCount += previousData.jumpExecutionCount;
   }

   @Override
   void reset()
   {
      super.reset();
      jumpExecutionCount = 0;
   }
}
