/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.suppress.staticinitializer;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;
import powermock.examples.suppress.staticinitializer.ExampleWithEvilStaticInitializer_JMockit_Test.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/DocumentationExamples/src/test/java/powermock/examples/suppress/staticinitializer/ExampleWithEvilStaticInitializerTest.java">PowerMock version</a>
 */
@UsingMocksAndStubs(MockExampleWithStaticInitializer.class)
public final class ExampleWithEvilStaticInitializer_JMockit_Test
{
   static class MockExampleWithStaticInitializer extends MockUp<ExampleWithEvilStaticInitializer>
   {
      @Mock static void $clinit() {}
   }

   @Test
   public void suppressStaticInitializer()
   {
      String message = "myMessage";
      ExampleWithEvilStaticInitializer tested = new ExampleWithEvilStaticInitializer(message);
      assertEquals(message, tested.getMessage());
   }
}
