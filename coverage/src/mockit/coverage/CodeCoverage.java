/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

import java.lang.instrument.*;
import java.security.*;
import java.util.*;
import java.io.*;

import mockit.coverage.data.*;
import mockit.coverage.modification.*;
import mockit.coverage.standalone.*;

public final class CodeCoverage implements ClassFileTransformer
{
   private static CodeCoverage instance;

   private final ClassModification classModification;
   private final OutputFileGenerator outputGenerator;

   public static void main(String[] args)
   {
      OutputFileGenerator generator = createOutputFileGenerator();
      generator.generateAggregateReportFromInputFiles(args);
   }

   private static OutputFileGenerator createOutputFileGenerator()
   {
      OutputFileGenerator generator = new OutputFileGenerator();
      CoverageData.instance().setWithCallPoints(generator.isWithCallPoints());
      return generator;
   }

   @SuppressWarnings("UnusedDeclaration")
   public CodeCoverage() { this(true); }

   ClassLoader classLoader = CodeCoverage.class.getClassLoader();
   private void loadClass(String cls) {
     try {
       Class aClass = classLoader.loadClass(cls);
       System.out.println("XLoaded : " + aClass.getName());
     } catch (ClassNotFoundException e) {
       e.printStackTrace();
     }
   }

   private ArrayList<String> readFile(String file) {
     File x = new File(file);
     ArrayList<String> list = new ArrayList<String>();
     if (x.exists()) {
       try {
       Scanner s = new Scanner(x);
       while (s.hasNext()){
         list.add(s.next());
       }
       s.close();
       } catch (FileNotFoundException e) {
       }
     }
     return list;
   }


   private CodeCoverage(final boolean generateOutputOnShutdown)
   {
      if (generateOutputOnShutdown && "none".equals(System.getProperty("jmockit-coverage-output"))) {
         throw new IllegalStateException("JMockit: coverage tool disabled");
      }

      classModification = new ClassModification();
      outputGenerator = createOutputFileGenerator();

      Runtime.getRuntime().addShutdownHook(new Thread() {
         @Override
         public void run()
         {
            System.out.println("----------------HERE--------------" + classLoader.getResource("").getPath());
            ArrayList<String> lst = readFile(".classes");
            for(String l : lst) {
              loadClass(l);
            }

            Startup.instrumentation().removeTransformer(CodeCoverage.this);

            if (generateOutputOnShutdown) {
               if (outputGenerator.isOutputToBeGenerated()) {
                  outputGenerator.generate();
               }

               new CoverageCheck().verifyThresholds();
            }
         }
      });
   }

   public static CodeCoverage create(boolean generateOutputOnShutdown)
   {
      instance = new CodeCoverage(generateOutputOnShutdown);
      return instance;
   }

   public static void resetConfiguration()
   {
      Startup.instrumentation().removeTransformer(instance);
      CoverageData.instance().clear();
      Startup.instrumentation().addTransformer(create(false));
   }

   public static void generateOutput(boolean resetState)
   {
      instance.outputGenerator.generate();

      if (resetState) {
         CoverageData.instance().reset();
      }
   }

   public byte[] transform(
      ClassLoader loader, String internalClassName, Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
      byte[] originalClassfile)
   {
      if (loader == null || classBeingRedefined != null || protectionDomain == null) {
         return null;
      }

      String className = internalClassName.replace('/', '.');

      return classModification.modifyClass(className, protectionDomain, originalClassfile);
   }
}
