/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.lines;

import java.io.*;
import java.util.*;

import mockit.coverage.*;
import mockit.coverage.data.*;

public final class PerFileLineCoverage implements PerFileCoverage
{
   private static final long serialVersionUID = 6318915843739466316L;

   public final SortedMap<Integer, LineCoverageData> lineToLineData = new TreeMap<Integer, LineCoverageData>();

   // Computed on demand:
   private transient int totalSegments;
   private transient int coveredSegments;

   public PerFileLineCoverage() { initializeCache(); }
   private void initializeCache() { totalSegments = coveredSegments = -1; }

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      initializeCache();
      in.defaultReadObject();
   }

   public LineCoverageData addLine(int line)
   {
      LineCoverageData lineData = lineToLineData.get(line);

      if (lineData == null) {
         lineData = new LineCoverageData();
         lineToLineData.put(line, lineData);
      }

      return lineData;
   }

   public void registerExecution(int line, CallPoint callPoint)
   {
      LineCoverageData lineData = lineToLineData.get(line);
      lineData.registerExecution(callPoint);
   }

   public void registerExecution(int line, int segment, boolean jumped, CallPoint callPoint)
   {
      LineCoverageData lineData = lineToLineData.get(line);
      lineData.registerExecution(segment, jumped, callPoint);
   }

   public int getTotalItems()
   {
      computeValuesIfNeeded();
      return totalSegments;
   }

   public int getCoveredItems()
   {
      computeValuesIfNeeded();
      return coveredSegments;
   }

   public int getCoveragePercentage()
   {
      computeValuesIfNeeded();
      return CoveragePercentage.calculate(coveredSegments, totalSegments);
   }

   private void computeValuesIfNeeded()
   {
      if (totalSegments >= 0) return;

      totalSegments = coveredSegments = 0;

      for (LineCoverageData line : lineToLineData.values()) {
         totalSegments += line.getNumberOfSegments();
         coveredSegments += line.getNumberOfCoveredSegments();
      }
   }

   public void reset()
   {
      for (LineCoverageData lineData : lineToLineData.values()) {
         lineData.reset();
      }

      initializeCache();
   }

   public void mergeInformation(PerFileLineCoverage previousCoverage)
   {
      Map<Integer, LineCoverageData> previousInfo = previousCoverage.lineToLineData;

      for (Map.Entry<Integer, LineCoverageData> lineAndInfo : lineToLineData.entrySet()) {
         Integer line = lineAndInfo.getKey();
         LineCoverageData previousLineInfo = previousInfo.get(line);

         if (previousLineInfo != null) {
            LineCoverageData lineInfo = lineAndInfo.getValue();
            lineInfo.addCountsFromPreviousTestRun(previousLineInfo);
         }
      }

      for (Map.Entry<Integer, LineCoverageData> lineAndInfo : previousInfo.entrySet()) {
         Integer line = lineAndInfo.getKey();

         if (!lineToLineData.containsKey(line)) {
            lineToLineData.put(line, lineAndInfo.getValue());
         }
      }
   }
}
