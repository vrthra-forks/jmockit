/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.lines;

import java.util.*;

import mockit.coverage.*;
import mockit.external.asm4.*;

/**
 * Coverage data gathered for a single executable line of code in a source file.
 */
public final class LineCoverageData extends LineSegmentData
{
   private static final long serialVersionUID = -6233980722802474992L;

   // Static data:
   private List<BranchCoverageData> branches;

   public int addBranch(Label jumpSource, Label jumpTarget)
   {
      if (branches == null) {
         branches = new ArrayList<BranchCoverageData>(4);
      }

      BranchCoverageData data = new BranchCoverageData(jumpSource, jumpTarget);
      branches.add(data);

      return branches.size() - 1;
   }

   public BranchCoverageData getBranchData(int index)
   {
      return branches.get(index);
   }

   public void registerExecution(int branchIndex, boolean jumped, CallPoint callPoint)
   {
      BranchCoverageData data = branches.get(branchIndex);

      if (jumped) {
         data.registerJumpExecution(callPoint);
      }
      else {
         data.registerNoJumpExecution(callPoint);
      }
   }

   public boolean containsBranches() { return branches != null; }
   public List<BranchCoverageData> getBranches() { return branches; }

   public int getNumberOfSegments()
   {
      return branches == null ? 1 : 1 + branches.size();
   }

   public int getNumberOfCoveredSegments()
   {
      if (unreachable) {
         return getNumberOfSegments();
      }

      if (executionCount == 0) {
         return 0;
      }

      if (branches == null) {
         return 1;
      }

      return getSegmentsCovered();
   }

   private int getSegmentsCovered()
   {
      int segmentsCovered = 1;

      for (BranchCoverageData branch : branches) {
         if (branch.isCovered()) {
            segmentsCovered++;
         }
      }

      return segmentsCovered;
   }

   public void addCountsFromPreviousTestRun(LineCoverageData previousData)
   {
      addExecutionCountAndCallPointsFromPreviousTestRun(previousData);

      if (containsBranches()) {
         for (int i = 0; i < branches.size(); i++) {
            BranchCoverageData segmentData = branches.get(i);
            BranchCoverageData previousSegmentData = previousData.branches.get(i);

            segmentData.addCountsFromPreviousTestRun(previousSegmentData);
         }
      }
   }

   @Override
   public void reset()
   {
      super.reset();

      if (branches != null) {
         for (BranchCoverageData branchData : branches) {
            branchData.reset();
         }
      }
   }
}
