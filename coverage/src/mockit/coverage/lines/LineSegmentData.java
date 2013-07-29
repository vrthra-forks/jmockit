/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.lines;

import java.io.*;
import java.util.*;

import mockit.coverage.*;

public class LineSegmentData implements Serializable
{
   private static final long serialVersionUID = -6233980722802474992L;

   // Static data:
   boolean unreachable;

   // Runtime data:
   int executionCount;
   private List<CallPoint> callPoints;

   public final boolean isUnreachable() { return unreachable; }
   public final void markAsUnreachable() { unreachable = true; }

   public final void registerExecution(CallPoint callPoint)
   {
      addCallPointIfAny(callPoint);
      executionCount++;
   }

   final void addCallPointIfAny(CallPoint callPoint)
   {
      if (callPoint != null) {
         if (callPoints == null) {
            callPoints = new ArrayList<CallPoint>();
         }

         callPoints.add(callPoint);
      }
   }

   public final boolean containsCallPoints() { return callPoints != null; }
   public final List<CallPoint> getCallPoints() { return callPoints; }

   public int getExecutionCount() { return executionCount; }
   public boolean isCovered() { return unreachable || executionCount > 0; }

   final void addExecutionCountAndCallPointsFromPreviousTestRun(LineSegmentData previousData)
   {
      executionCount += previousData.executionCount;

      if (previousData.containsCallPoints()) {
         if (containsCallPoints()) {
            callPoints.addAll(0, previousData.callPoints);
         }
         else {
            callPoints = previousData.callPoints;
         }
      }
   }

   void reset() { executionCount = 0; }
}