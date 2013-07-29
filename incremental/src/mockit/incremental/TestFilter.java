/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.incremental;

import java.lang.reflect.*;
import java.io.*;
import java.util.*;

public final class TestFilter
{
   private final Properties coverageMap;
   private long lastTestRun;

   public TestFilter(Properties coverageMap)
   {
      this.coverageMap = coverageMap;
   }

   public boolean shouldIgnoreTestInCurrentTestRun(Method testMethod)
   {
      String testClassName = testMethod.getDeclaringClass().getName();
      String testName = testClassName + '.' + testMethod.getName();
      String coverageInfo = coverageMap.getProperty(testName);

      if (coverageInfo != null && coverageInfo.indexOf(',') > 0) {
         String[] lastRunAndSourcesCovered = coverageInfo.split(",");
         lastTestRun = Long.parseLong(lastRunAndSourcesCovered[0]);
         String testClassFileName = testClassName.replace('.', '/') + ".class";

         if (
            hasClassFileChangedSinceLastTestRun(testClassFileName) ||
            hasTestedCodeChangedSinceLastTestRun(lastRunAndSourcesCovered)
         ) {
            coverageMap.remove(testName);
            return false;
         }

         return true;
      }

      return false;
   }

   private boolean hasClassFileChangedSinceLastTestRun(String sourceFileName)
   {
      String classFileName = sourceFileName.replace(".java", ".class");
      String classFilePath = getClass().getResource('/' + classFileName).getPath();
      File classFile = new File(classFilePath);

      return classFile.lastModified() > lastTestRun;
   }

   private boolean hasTestedCodeChangedSinceLastTestRun(String[] sourceFilesCovered)
   {
      for (int i = 1; i < sourceFilesCovered.length; i++) {
         if (hasClassFileChangedSinceLastTestRun(sourceFilesCovered[i])) {
            return true;
         }
      }

      return false;
   }
}
