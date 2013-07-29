/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import java.io.*;

import mockit.coverage.data.*;

final class AccretionFile
{
   private final File outputFile;
   private final CoverageData newData;

   AccretionFile(String outputDir, CoverageData newData)
   {
      String parentDir = outputDir.length() == 0 ? null : outputDir;
      outputFile = new File(parentDir, "coverage.ser");

      newData.fillLastModifiedTimesForAllClassFiles();
      this.newData = newData;
   }

   void mergeDataFromExistingFileIfAny() throws IOException
   {
      if (outputFile.exists()) {
         CoverageData previousData = CoverageData.readDataFromFile(outputFile);
         newData.merge(previousData);
      }
   }

   void generate() throws IOException
   {
      newData.writeDataToFile(outputFile);
      System.out.println("JMockit: Coverage data written to " + outputFile.getCanonicalPath());
   }
}
