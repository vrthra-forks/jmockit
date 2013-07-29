package coverageExperiments;

import org.junit.*;

import static org.junit.Assert.*;

public class CoverMeSimpleTest
{
   @Test
   public void testSimple() throws Exception
   {
      int result = new CoverMeSimple().simple(1, 2);
      assertEquals(4, result);
   }
}
