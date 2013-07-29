/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting;

import java.io.*;
import java.security.*;

final class StaticFiles
{
   private long lastModifiedTimeOfCoverageJar;

   void copyToOutputDir(String outputDir, boolean forSourceFilePages) throws IOException
   {
      copyFile(outputDir, "coverage.css");
      copyFile(outputDir, "coverage.js");
      copyFile(outputDir, "logo.png");

      if (forSourceFilePages) {
         copyFile(outputDir, "prettify.js");
      }
   }

   private void copyFile(String outputDir, String fileName) throws IOException
   {
      File outputFile = new File(outputDir, fileName);

      if (outputFile.exists() && outputFile.lastModified() > getLastModifiedTimeOfCoverageJar()) {
         return;
      }

      OutputStream output = new BufferedOutputStream(new FileOutputStream(outputFile));
      InputStream input = new BufferedInputStream(StaticFiles.class.getResourceAsStream(fileName));

      try {
         int b;

         while ((b = input.read()) != -1) {
            output.write(b);
         }
      }
      finally {
         try {
            input.close();
         }
         finally {
            output.close();
         }
      }
   }

   private long getLastModifiedTimeOfCoverageJar()
   {
      if (lastModifiedTimeOfCoverageJar == 0) {
         CodeSource codeSource = getClass().getProtectionDomain().getCodeSource();

         if (codeSource == null) {
            lastModifiedTimeOfCoverageJar = -1;
         }
         else {
            String pathToThisJar = codeSource.getLocation().getPath();
            lastModifiedTimeOfCoverageJar = new File(pathToThisJar).lastModified();
         }
      }

      return lastModifiedTimeOfCoverageJar;
   }
}