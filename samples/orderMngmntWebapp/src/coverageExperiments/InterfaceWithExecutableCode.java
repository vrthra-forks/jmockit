package coverageExperiments;

import java.util.*;

public interface InterfaceWithExecutableCode
{
   /**
    * This constant field is initialized at runtime; therefore the interface contains executable
    * code which can be exercised by a test.
    */
   int N = 1 + new Random().nextInt(10);

   void doSomething();
}