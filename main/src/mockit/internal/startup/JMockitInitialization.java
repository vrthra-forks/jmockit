/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.internal.startup;

import java.io.*;

import mockit.integration.junit3.internal.*;
import mockit.integration.junit4.internal.*;
import mockit.integration.testng.internal.*;
import mockit.internal.*;
import mockit.internal.annotations.*;
import mockit.internal.util.*;

final class JMockitInitialization
{
   private static final String[] NO_STUBBING_FILTERS = {};
   private final StartupConfiguration config;

   JMockitInitialization() throws IOException { config = new StartupConfiguration(); }

   void initialize(boolean initializeTestNG)
   {
      MockingBridge.preventEventualClassLoadingConflicts();
      loadInternalStartupMocksForJUnitIntegration();

      if (initializeTestNG && MockTestNG.hasDependenciesInClasspath()) {
         setUpInternalStartupMock(MockTestNG.class);
      }

      loadExternalToolsIfAny();
      stubOutClassesIfAny();
      setUpStartupMocksIfAny();
   }

   private void loadInternalStartupMocksForJUnitIntegration()
   {
      if (setUpInternalStartupMock(TestSuiteDecorator.class)) {
         try {
            setUpInternalStartupMock(MockTestCase.class);
         }
         catch (VerifyError ignore) {
            // For some reason, this error occurs when running TestNG tests from Maven.
         }

         if (!setUpInternalStartupMock(RunNotifierDecorator.class)) return;
         if (!setUpInternalStartupMock(BlockJUnit4ClassRunnerDecorator.class)) return;
         setUpInternalStartupMock(MockFrameworkMethod.class);
      }
   }

   private boolean setUpInternalStartupMock(Class<?> mockClass)
   {
      // The startup mock is ignored if the necessary third-party class files are not in the classpath.
      try {
         applyMockClass(mockClass);
         return true;
      }
      catch (TypeNotPresentException ignore) { return false; }
      catch (NoClassDefFoundError ignore) { return false; }
   }

   private void applyMockClass(Class<?> mockClass)
   {
      MockClassSetup setup;

      //noinspection UnnecessaryFullyQualifiedName
      if (mockit.MockUp.class.isAssignableFrom(mockClass)) {
         Object mockUp = ConstructorReflection.newInstance(mockClass);
         setup = new MockClassSetup(mockUp);
      }
      else {
         setup = new MockClassSetup(mockClass);
      }

      setup.setUpStartupMock();
   }

   private void loadExternalToolsIfAny()
   {
      for (String toolClassName : config.externalTools) {
         try {
            new ToolLoader(toolClassName).loadTool();
         }
         catch (Throwable unexpectedFailure) {
            StackTrace.filterStackTrace(unexpectedFailure);
            unexpectedFailure.printStackTrace();
         }
      }
   }

   private void stubOutClassesIfAny()
   {
      for (String stubbing : config.classesToBeStubbedOut) {
         int p = stubbing.indexOf('#');
         String realClassName = stubbing;
         String[] filters = NO_STUBBING_FILTERS;

         if (p > 0) {
            realClassName = stubbing.substring(0, p);
            filters = stubbing.substring(p + 1).split("\\|");
         }

         Class<?> realClass = ClassLoad.loadClass(realClassName.trim());
         new ClassStubbing(realClass, filters).stubOutAtStartup();
      }
   }

   private void setUpStartupMocksIfAny()
   {
      for (String mockClassName : config.mockClasses) {
         try {
            Class<?> mockClass = ClassLoad.loadClass(mockClassName);
            applyMockClass(mockClass);
         }
         catch (UnsupportedOperationException ignored) {}
         catch (Throwable unexpectedFailure) {
            StackTrace.filterStackTrace(unexpectedFailure);
            unexpectedFailure.printStackTrace();
         }
      }
   }
}
