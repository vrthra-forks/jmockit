/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import org.junit.*;
import static org.junit.Assert.*;

public final class ReentrantMockedMethodsTest
{
   static class Factory1 { static Factory1 create() { return null; } }
   static class Client1 { static final Factory1 factory = Factory1.create(); }

   @Test
   public void callMethodOnFirstMockedClassDuringStaticInitializationOfSecondMockedClass(final Factory1 mock1)
   {
      new NonStrictExpectations() {{ Factory1.create(); result = mock1; }};
      new NonStrictExpectations() { @Mocked Client1 mock2; };
      assertNotNull(Client1.factory);
   }

   static class Factory2 { static Factory2 create() { return null; } }
   static class Client2 { OtherClient2 getOtherClient() { return null; } }
   static class OtherClient2 { static final Factory2 F = Factory2.create(); }

   @Test
   public void cascadeDuringStaticInitializationOfCascadingClass(@Cascading Factory2 mock1, @Cascading Client2 mock2)
   {
      assertNotNull(mock2.getOtherClient());
      assertNotNull(OtherClient2.F);
   }
}
