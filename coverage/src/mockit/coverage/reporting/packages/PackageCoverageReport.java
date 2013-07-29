/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.packages;

import java.io.*;
import java.util.*;

import mockit.coverage.*;
import mockit.coverage.data.*;
import mockit.coverage.standalone.*;

final class PackageCoverageReport extends ListWithFilesAndPercentages
{
   private final Map<String, FileCoverageData> filesToFileData;
   private final Collection<String> sourceFilesNotFound;
   private final char[] fileNameWithSpaces;
   private String filePath;

   PackageCoverageReport(
      PrintWriter output, Collection<String> sourceFilesNotFound, Map<String, FileCoverageData> filesToFileData,
      Collection<List<String>> allSourceFileNames)
   {
      super(output, "          ");
      this.sourceFilesNotFound = sourceFilesNotFound;
      this.filesToFileData = filesToFileData;
      fileNameWithSpaces = new char[maximumSourceFileNameLength(allSourceFileNames)];
   }

   private int maximumSourceFileNameLength(Collection<List<String>> allSourceFileNames)
   {
      int maxLength = 0;

      for (List<String> files : allSourceFileNames) {
         for (String fileName : files) {
            int n = fileName.length();

            if (n > maxLength) {
               maxLength = n;
            }
         }
      }

      return maxLength;
   }

   @Override
   protected void writeMetricsForFile(String packageName, String fileName)
   {
      filePath = packageName.length() == 0 ? fileName : packageName + '/' + fileName;
      final FileCoverageData fileData = filesToFileData.get(filePath);

      if (fileData == null && Startup.isTestRun() && isSourceFileWithTestCodeOnly(fileName)) {
         return;
      }

      writeRowStart();
      printIndent();
      output.write("  <td class='file'>");

      int fileNameLength = buildFileNameWithTrailingSpaces(fileName);

      if (fileData == null) {
         writeTableCellsWithFileNameAndUnknownCoverageMetrics();
      }
      else {
         writeTableCellWithFileName(fileNameLength);

         Metrics.performAction(new Metrics.Action() {
            public void perform(Metrics metric)
            {
               writeCodeCoverageMetricForFile(metric, fileData.coverageInfos[metric.ordinal()]);
            }
         });
      }

      writeRowClose();
   }

   private boolean isSourceFileWithTestCodeOnly(String fileName)
   {
      int p = fileName.lastIndexOf('.');
      String nameNoExt = p > 0 ? fileName.substring(0, p) : fileName;
      return nameNoExt.endsWith("Test");
   }

   private int buildFileNameWithTrailingSpaces(String fileName)
   {
      int n = fileName.length();

      fileName.getChars(0, n, fileNameWithSpaces, 0);
      Arrays.fill(fileNameWithSpaces, n, fileNameWithSpaces.length, ' ');
      
      return n;
   }

   private void writeTableCellsWithFileNameAndUnknownCoverageMetrics()
   {
      output.write(fileNameWithSpaces);
      output.println("</td>");

      printIndent();
      output.print("  <td colspan='");
      output.print(Metrics.amountActive());
      output.println("' class='unknown'>?</td>");
   }

   private void writeTableCellWithFileName(int fileNameLen)
   {
      if (sourceFilesNotFound == null || sourceFilesNotFound.contains(filePath)) {
         output.write(fileNameWithSpaces);
      }
      else {
         output.write("<a target='_blank' href='");
         int p = filePath.lastIndexOf('.');
         output.write(filePath.substring(0, p));
         output.write(".html'>");
         output.write(fileNameWithSpaces, 0, fileNameLen);
         output.write("</a>");
         output.write(fileNameWithSpaces, fileNameLen, fileNameWithSpaces.length - fileNameLen);
      }

      output.println("</td>");
   }

   private void writeCodeCoverageMetricForFile(Metrics metric, PerFileCoverage coverageInfo)
   {
      int percentage = coverageInfo.getCoveragePercentage();
      int covered = coverageInfo.getCoveredItems();
      int total = coverageInfo.getTotalItems();

      coveredItems[metric.ordinal()] += covered;
      totalItems[metric.ordinal()] += total;

      printCoveragePercentage(metric, covered, total, percentage);
   }

   @Override
   protected void writeClassAttributeForCoveragePercentageCell() {}
}