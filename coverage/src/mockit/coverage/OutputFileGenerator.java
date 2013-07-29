/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import java.io.*;

import mockit.coverage.data.*;
import mockit.coverage.reporting.*;
import mockit.coverage.standalone.*;

final class OutputFileGenerator
{
   private static final String COVERAGE_PREFIX = "jmockit-coverage-";
   private static final String[] ALL_SOURCE_DIRS = new String[0];

   private final String[] outputFormats;
   private final String outputDir;
   private final String[] sourceDirs;

   OutputFileGenerator()
   {
      outputFormats = getOutputFormat();
      outputDir = getCoverageProperty("outputDir");

      String commaSeparatedDirs = System.getProperty(COVERAGE_PREFIX + "srcDirs");

      if (commaSeparatedDirs == null) {
         sourceDirs = Startup.isTestRun() ? ALL_SOURCE_DIRS : null;
      }
      else if (commaSeparatedDirs.length() == 0) {
         sourceDirs = null;
      }
      else {
         sourceDirs = commaSeparatedDirs.split(",");
      }
   }

   private String[] getOutputFormat()
   {
      String format = getCoverageProperty("output");
      return format.length() == 0 ? new String[] {"html-nocp"} : format.trim().split("\\s*,\\s*|\\s+");
   }

   private String getCoverageProperty(String suffix)
   {
      return System.getProperty(COVERAGE_PREFIX + suffix, "");
   }

   boolean isOutputToBeGenerated()
   {
      return isOutputWithCallpointsToBeGenerated() || hasOutputFormat("html-nocp");
   }

   private boolean isOutputWithCallpointsToBeGenerated()
   {
      return hasOutputFormat("html") || hasOutputFormat("serial") || hasOutputFormat("merge");
   }

   boolean isWithCallPoints()
   {
      return Startup.isTestRun() && isOutputWithCallpointsToBeGenerated() && !hasOutputFormat("html-nocp");
   }

   private boolean hasOutputFormat(String format)
   {
      for (String outputFormat : outputFormats) {
         if (format.equals(outputFormat)) {
            return true;
         }
      }

      return false;
   }

   void generate()
   {
      CoverageData coverageData = CoverageData.instance();

      if (coverageData.isEmpty()) {
         System.out.println(
            "JMockit: No classes were instrumented for coverage; please make sure that classes selected for coverage " +
            "have been compiled with debug information.");
         return;
      }

      createOutputDirIfSpecifiedButNotExists();

      try {
         generateAccretionDataFileIfRequested(coverageData);
         generateHTMLReportIfRequested(coverageData);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   void generateAggregateReportFromInputFiles(String[] inputPaths)
   {
      createOutputDirIfSpecifiedButNotExists();

      try {
         CoverageData coverageData = new DataFileMerging(inputPaths).merge();
         generateHTMLReportIfRequested(coverageData);
      }
      catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

   private void createOutputDirIfSpecifiedButNotExists()
   {
      if (outputDir.length() > 0) {
         File outDir = new File(outputDir);

         if (!outDir.exists()) {
            boolean dirCreated = outDir.mkdir();
            assert dirCreated : "Failed to create specified output dir: " + outputDir;
         }
      }
   }

   private void generateAccretionDataFileIfRequested(CoverageData newData) throws IOException
   {
      if (hasOutputFormat("serial")) {
         new AccretionFile(outputDir, newData).generate();
      }
      else if (hasOutputFormat("merge")) {
         AccretionFile accretionFile = new AccretionFile(outputDir, newData);
         accretionFile.mergeDataFromExistingFileIfAny();
         accretionFile.generate();
      }
   }

   private void generateHTMLReportIfRequested(CoverageData coverageData) throws IOException
   {
      if (hasOutputFormat("html-nocp")) {
         new BasicCoverageReport(outputDir, sourceDirs, coverageData).generate();
      }
      else if (hasOutputFormat("html")) {
         new FullCoverageReport(outputDir, sourceDirs, coverageData).generate();
      }
   }
}
