/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.sourceFiles;

import java.io.*;
import java.util.*;

public final class InputFile
{
   final String filePath;
   private final File sourceFile;
   private final BufferedReader input;

   public InputFile(List<File> sourceDirs, String filePath) throws FileNotFoundException
   {
      this.filePath = filePath;
      sourceFile = findSourceFile(sourceDirs, filePath);
      input = sourceFile == null ? null : new BufferedReader(new FileReader(sourceFile));
   }

   private File findSourceFile(List<File> sourceDirs, String filePath)
   {
      int p = filePath.indexOf('/');
      String topLevelPackage = p < 0 ? "" : filePath.substring(0, p);

      for (File sourceDir : sourceDirs) {
         File file = getSourceFile(sourceDir, topLevelPackage, filePath);

         if (file != null) {
            return file;
         }
      }

      return null;
   }

   private File getSourceFile(File sourceDir, String topLevelPackage, String filePath)
   {
      File file = new File(sourceDir, filePath);

      if (file.exists()) {
         return file;
      }

      File[] subDirs = sourceDir.listFiles();

      for (File subDir : subDirs) {
         if (subDir.isDirectory() && !subDir.isHidden() && !subDir.getName().equals(topLevelPackage)) {
            file = getSourceFile(subDir, topLevelPackage, filePath);

            if (file != null) {
               return file;
            }
         }
      }

      return null;
   }

   public boolean wasFileFound() { return sourceFile != null; }
   String getSourceFileName() { return sourceFile.getName(); }

   String getSourceFilePath()
   {
      String path = sourceFile.getPath();
      return path.startsWith("..") ? path.substring(3) : path;
   }

   String nextLine() throws IOException { return input.readLine(); }
   void close() throws IOException { input.close(); }
}
