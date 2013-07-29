/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.integration.junit4;

import java.lang.annotation.*;
import java.util.*;

import org.junit.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.model.*;
import static org.junit.Assert.*;

import mockit.*;

@RunWith(JUnit4CustomRunnerTest.CustomRunner.class)
public final class JUnit4CustomRunnerTest
{
   private static final boolean ANNOTATED_MOCK_PARAMETERS_ONLY =
      "annotated".equals(System.getProperty("jmockit-mockParameters"));

   public static final class CustomRunner extends BlockJUnit4ClassRunner
   {
      public CustomRunner(Class<?> klass) throws InitializationError { super(klass); }

      @Override
      protected void validatePublicVoidNoArgMethods(
         Class<? extends Annotation> annotation, boolean isStatic, List<Throwable> errors)
      {
      }
   }

   @Test
   public void withAnnotatedParameters(@Mocked Runnable runnable, @Injectable Dependency dep)
   {
      assertNotNull(runnable);
      assertNotNull(dep);
   }

   @Test
   public void withNonAnnotatedParameters(Runnable runnable, Dependency dep)
   {
      if (ANNOTATED_MOCK_PARAMETERS_ONLY) {
         assertNull(runnable);
         assertNull(dep);
      }
      else {
         assertNotNull(runnable);
         assertNotNull(dep);
      }

      new Expectations() {
         Dependency mock;

         {
            assertNotNull(mock);
         }
      };
   }
}
