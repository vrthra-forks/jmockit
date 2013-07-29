/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.util.*;
import static java.io.File.separatorChar;

final class ProcessBuilderCreation
{
   final ProcessBuilder processBuilder;

   ProcessBuilderCreation()
   {
      List<String> commandAndParameters = buildCommandList();
      processBuilder = new ProcessBuilder(commandAndParameters);
      processBuilder.redirectErrorStream(true);
   }

   private List<String> buildCommandList()
   {
      List<String> commandAndParameters = new ArrayList<String>();

      String javaExecutablePath = getPathToJavaExecutable();
      commandAndParameters.add(javaExecutablePath);

      String assertionStatusParameter = getAssertionStatusParameter();

      if (assertionStatusParameter != null) {
         commandAndParameters.add(assertionStatusParameter);
      }

      String classPath = System.getProperty("java.class.path");
      commandAndParameters.add("-cp");
      commandAndParameters.add(classPath);

      commandAndParameters.add(SingleCoreRunner.class.getName());
      return commandAndParameters;
   }

   private String getPathToJavaExecutable()
   {
      String home = System.getProperty("java.home");
      return home.substring(0, home.lastIndexOf(separatorChar) + 1) + "bin" + separatorChar + "java";
   }

   private String getAssertionStatusParameter()
   {
      // Force the assertion status to be initialized so that it can be read:
      ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
      systemClassLoader.setClassAssertionStatus(getClass().getName(), false);

      Boolean assertionStatus = Utilities.getField(ClassLoader.class, systemClassLoader, "defaultAssertionStatus");
      return assertionStatus ? "-ea" : null;
   }
}
