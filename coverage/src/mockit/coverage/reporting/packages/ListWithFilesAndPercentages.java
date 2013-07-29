/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.packages;

import java.io.*;
import java.util.*;

import mockit.coverage.*;

abstract class ListWithFilesAndPercentages
{
   protected final PrintWriter output;
   private final String baseIndent;
   final int[] totalItems = new int[Metrics.values().length];
   final int[] coveredItems = new int[Metrics.values().length];

   protected ListWithFilesAndPercentages(PrintWriter output, String baseIndent)
   {
      this.output = output;
      this.baseIndent = baseIndent;
   }

   final void writeMetricsForEachFile(String packageName, List<String> fileNames)
   {
      if (fileNames.isEmpty()) {
         return;
      }

      Collections.sort(fileNames);
      Arrays.fill(totalItems, 0);
      Arrays.fill(coveredItems, 0);

      for (String fileName : fileNames) {
         writeMetricsForFile(packageName, fileName);
      }
   }

   protected final void writeRowStart()
   {
      printIndent();
      output.println("<tr>");
   }

   protected final void writeRowClose()
   {
      printIndent();
      output.println("</tr>");
   }

   final void printIndent() { output.write(baseIndent); }

   protected abstract void writeMetricsForFile(String packageName, String fileName);

   final void printCoveragePercentage(Metrics metric, int covered, int total, int percentage)
   {
      printIndent();
      output.write("  <td ");

      if (total > 0) {
         writeRowCellWithCoveragePercentage(metric, covered, total, percentage);
      }
      else {
         output.write("class='nocode'>N/A");
      }

      output.println("</td>");
   }

   private void writeRowCellWithCoveragePercentage(Metrics metric, int covered, int total, int percentage)
   {
      writeClassAttributeForCoveragePercentageCell();
      output.write("style='background-color:#");
      output.write(CoveragePercentage.percentageColor(covered, total));
      output.write("' title='");
      output.write(metric.itemName());
      output.write(": ");
      output.print(covered);
      output.write('/');
      output.print(total);
      output.write("'>");
      writePercentageValue(covered, total, percentage);
      output.print("%");
   }

   protected abstract void writeClassAttributeForCoveragePercentageCell();

   private void writePercentageValue(int covered, int total, int percentage)
   {
      if (percentage < 100) {
         output.print(percentage);
      }
      else if (covered == total) {
         output.print("100");
      }
      else {
         output.print(">99");
      }
   }
}