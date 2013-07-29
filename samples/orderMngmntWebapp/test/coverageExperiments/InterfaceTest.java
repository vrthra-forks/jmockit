package coverageExperiments;

import org.junit.*;

import static org.junit.Assert.assertTrue;

public final class InterfaceTest
{
   @Test
   public void useInterfaceWithNoExecutableCode(InterfaceWithNoExecutableCode itf)
   {
      itf.doSomething();
   }

   @Test
   public void useInterfaceWithExecutableCode()
   {
      assertTrue(InterfaceWithExecutableCode.N > 0);
   }
}
