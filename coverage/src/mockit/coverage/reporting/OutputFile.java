/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting;

import java.io.*;
import java.util.regex.*;

public final class OutputFile extends PrintWriter
{
   private static final Pattern PATH_SEPARATOR = Pattern.compile("/");

   private final String relPathToOutDir;
   private final boolean withPrettyPrint;

   public OutputFile(File file) throws IOException
   {
      super(new FileWriter(file));
      relPathToOutDir = "";
      withPrettyPrint = false;
   }

   public OutputFile(String outputDir, String sourceFilePath) throws IOException
   {
      super(new FileWriter(getOutputFileCreatingDirIfNeeded(outputDir, sourceFilePath)));
      relPathToOutDir = getRelativeSubPathToOutputDir(sourceFilePath);
      withPrettyPrint = true;
   }

   private static File getOutputFileCreatingDirIfNeeded(String outputDir, String sourceFilePath)
   {
      int p = sourceFilePath.lastIndexOf('.');
      String outputFileName = sourceFilePath.substring(0, p) + ".html";
      File outputFile = new File(outputDir, outputFileName);
      File parentDir = outputFile.getParentFile();

      if (!parentDir.exists()) {
         boolean outputDirCreated = parentDir.mkdirs();
         assert outputDirCreated : "Failed to create output dir: " + outputDir;
      }

      return outputFile;
   }

   private static String getRelativeSubPathToOutputDir(String filePath)
   {
      StringBuilder cssRelPath = new StringBuilder();
      int n = PATH_SEPARATOR.split(filePath).length;

      for (int i = 1; i < n; i++) {
         cssRelPath.append("../");
      }

      return cssRelPath.toString();
   }

   public void writeCommonHeader(String subTitle)
   {
      String titleSuffix = subTitle == null ? "" : " - " + subTitle;

      println("<!DOCTYPE html>");
      println("<html>");
      println("<head>");
      println("  <title>JMockit Coverage Report" + titleSuffix + "</title>");
      println("  <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'/>");
      println("  <link rel='stylesheet' type='text/css' href='" + relPathToOutDir + "coverage.css'/>");
      println("  <script type='text/javascript' src='" + relPathToOutDir + "coverage.js'></script>");

      if (withPrettyPrint) {
         println("  <script type='text/javascript' src='" + relPathToOutDir + "prettify.js'></script>");
      }

      println("</head>");
      println(withPrettyPrint ? "<body onload='prettyPrint()'>" : "<body>");
   }

   public void writeCommonFooter()
   {
      println("</body>");
      println("</html>");
   }
}
