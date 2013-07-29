/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.lineCoverage;

import java.util.*;

import mockit.coverage.lines.*;
import mockit.coverage.reporting.*;
import mockit.coverage.reporting.parsing.*;

final class LineSegmentsFormatter
{
   private final ListOfCallPoints listOfCallPoints;
   private final StringBuilder line;
   private int lineNumber;

   // Helper fields:
   private LineElement element;
   private int segmentIndex;
   private LineSegmentData segmentData;

   LineSegmentsFormatter(boolean withCallPoints, StringBuilder line)
   {
      listOfCallPoints = withCallPoints ? new ListOfCallPoints() : null;
      this.line = line;
   }

   void formatSegments(LineParser lineParser, LineCoverageData lineData)
   {
      lineNumber = lineParser.getNumber();

      List<BranchCoverageData> branchData = lineData.getBranches();
      int numSegments = 1 + branchData.size();

      element = lineParser.getInitialElement().appendUntilNextCodeElement(line);

      segmentIndex = 0;
      segmentData = lineData;
      appendUntilFirstElementAfterNextBranchingPoint();

      for (segmentIndex = 1; element != null && segmentIndex < numSegments; segmentIndex++) {
         segmentData = branchData.get(segmentIndex - 1);

         element = element.appendUntilNextCodeElement(line);
         appendUntilFirstElementAfterNextBranchingPoint();
      }

      line.append("</pre>");

      if (hasCallPointsForSegment()) {
         line.append(listOfCallPoints.getContents());
      }
   }

   private void appendUntilFirstElementAfterNextBranchingPoint()
   {
      if (element != null) {
         LineElement firstElement = element;
         element = element.findNextBranchingPoint();

         appendToFormattedLine(firstElement);

         if (element != null && element.isBranchingElement()) {
            line.append(element.getText());
            element = element.getNext();
         }
      }
   }

   private void appendToFormattedLine(LineElement firstElement)
   {
      if (firstElement == element) {
         return;
      }

      appendStartTag();
      firstElement.appendAllBefore(line, element);
      appendEndTag();
   }

   private void appendStartTag()
   {
      line.append("<span id='l").append(lineNumber).append('s').append(segmentIndex).append("' ");

      appendTooltipWithExecutionCounts();

      if (segmentData.isCovered()) {
         line.append("class='covered");

         if (hasCallPointsForSegment()) {
            line.append(" cp' onclick='showHide(this,").append(segmentIndex).append(')');
         }

         line.append("'>");
      }
      else {
         line.append("class='uncovered'>");
      }
   }

   private void appendTooltipWithExecutionCounts()
   {
      line.append("title='Executions: ").append(segmentData.getExecutionCount()).append("' ");
   }

   private void appendEndTag()
   {
      int i = line.length() - 1;

      while (Character.isWhitespace(line.charAt(i))) {
         i--;
      }
      
      line.insert(i + 1, "</span>");

      if (hasCallPointsForSegment()) {
         listOfCallPoints.insertListOfCallPoints(segmentData.getCallPoints());
      }
   }

   private boolean hasCallPointsForSegment() { return listOfCallPoints != null && segmentData.containsCallPoints(); }
}
