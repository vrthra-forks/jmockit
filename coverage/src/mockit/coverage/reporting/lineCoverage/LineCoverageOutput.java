/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.lineCoverage;

import java.io.*;
import java.util.*;

import mockit.coverage.lines.*;
import mockit.coverage.reporting.parsing.*;

public final class LineCoverageOutput
{
   private final PrintWriter output;
   private final Map<Integer, LineCoverageData> lineToLineData;
   private final LineCoverageFormatter lineCoverageFormatter;
   private LineCoverageData lineData;

   public LineCoverageOutput(PrintWriter output, Map<Integer, LineCoverageData> lineToLineData, boolean withCallPoints)
   {
      this.output = output;
      this.lineToLineData = lineToLineData;
      lineCoverageFormatter = new LineCoverageFormatter(withCallPoints);
   }

   public boolean writeLineWithCoverageInfo(LineParser lineParser)
   {
      lineData = lineToLineData.get(lineParser.getNumber());

      if (lineData == null) {
         return false;
      }

      writeLineExecutionCount();
      writeExecutableCode(lineParser);
      return true;
   }

   private void writeLineExecutionCount()
   {
      output.write("<td class='count'>");
      output.print(lineData.getExecutionCount());
      output.println("</td>");
   }

   private void writeExecutableCode(LineParser lineParser)
   {
      String formattedLine = lineCoverageFormatter.format(lineParser, lineData);
      output.write("      <td>");
      output.write(formattedLine);
      output.println("</td>");
   }
}
