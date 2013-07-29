/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.pathCoverage;

import java.io.*;
import java.util.*;

import mockit.coverage.*;
import mockit.coverage.paths.*;

public final class PathCoverageOutput
{
   private final PrintWriter output;
   private final PathCoverageFormatter pathFormatter;
   private final Iterator<MethodCoverageData> nextMethod;

   // Helper fields:
   private MethodCoverageData currentMethod;

   public PathCoverageOutput(PrintWriter output, Collection<MethodCoverageData> methods)
   {
      this.output = output;
      pathFormatter = new PathCoverageFormatter(output);
      nextMethod = methods.iterator();
      moveToNextMethod();
   }

   private void moveToNextMethod()
   {
      currentMethod = nextMethod.hasNext() ? nextMethod.next() : null;
   }

   public void writePathCoverageInfoIfLineStartsANewMethodOrConstructor(int lineNumber)
   {
      if (currentMethod != null && lineNumber == currentMethod.getFirstLineInBody()) {
         writePathCoverageInformationForMethod();
         moveToNextMethod();
      }
   }

   private void writePathCoverageInformationForMethod()
   {
      List<Path> paths = currentMethod.getPaths();

      if (paths.size() > 1) {
         writeHeaderForAllPaths();
         pathFormatter.writeInformationForEachPath(paths);
         writeFooterForAllPaths();
      }
   }

   private void writeHeaderForAllPaths()
   {
      int coveredPaths = currentMethod.getCoveredPaths();
      int totalPaths = currentMethod.getTotalPaths();

      output.println("    <tr>");
      output.write("      <td></td><td class='count'>");
      output.print(currentMethod.getExecutionCount());
      output.println("</td>");
      output.println("      <td class='paths'>");
      output.write("        <span style='cursor:default; background-color:#");
      output.write(CoveragePercentage.percentageColor(coveredPaths, totalPaths));
      output.write("' onclick='hidePath()'>Path coverage: ");
      output.print(coveredPaths);
      output.print('/');
      output.print(totalPaths);
      output.println("</span>");
   }

   private void writeFooterForAllPaths()
   {
      output.println("      </td>");
      output.println("    </tr>");
   }
}
