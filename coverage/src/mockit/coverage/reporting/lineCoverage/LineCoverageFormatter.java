/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.lineCoverage;

import mockit.coverage.lines.*;
import mockit.coverage.reporting.*;
import mockit.coverage.reporting.parsing.*;

final class LineCoverageFormatter
{
   private final StringBuilder formattedLine;
   private final LineSegmentsFormatter segmentsFormatter;
   private final ListOfCallPoints listOfCallPoints;
   private LineCoverageData lineData;

   LineCoverageFormatter(boolean withCallPoints)
   {
      formattedLine = new StringBuilder(200);
      segmentsFormatter = new LineSegmentsFormatter(withCallPoints, formattedLine);
      listOfCallPoints = withCallPoints ? new ListOfCallPoints() : null;
   }

   String format(LineParser lineParser, LineCoverageData lineData)
   {
      this.lineData = lineData;

      formattedLine.setLength(0);
      formattedLine.append("<pre class='prettyprint");

      if (lineData.containsBranches()) {
         formatLineWithMultipleSegments(lineParser);
      }
      else {
         formatLineWithSingleSegment(lineParser);
      }

      return formattedLine.toString();
   }

   private void formatLineWithMultipleSegments(LineParser lineParser)
   {
      formattedLine.append(" jmp'>");
      segmentsFormatter.formatSegments(lineParser, lineData);
   }

   private void formatLineWithSingleSegment(LineParser lineParser)
   {
      formattedLine.append(lineData.isCovered() ? " covered" : " uncovered");

      boolean lineWithCallPoints = listOfCallPoints != null && lineData.containsCallPoints();

      if (lineWithCallPoints) {
         formattedLine.append(" cp' onclick='showHide(this)");
      }

      formattedLine.append("' id='l").append(lineParser.getNumber()).append("s0'>");
      formattedLine.append(lineParser.getInitialElement().toString()).append("</pre>");

      if (lineWithCallPoints) {
         listOfCallPoints.insertListOfCallPoints(lineData.getCallPoints());
         formattedLine.append(listOfCallPoints.getContents());
      }
   }
}
