package coverageExperiments;

/**
 * This class is intentionally not exercised at all by any test.
 * <p/>
 * It should appear in the report as having unknown coverage, without a defined percentage for
 * either metric.
 */
public class ClassNotExercised
{
   public boolean doSomething(int i, String s)
   {
      if (i > 0) {
         System.out.println(s);
      }

      return s.length() > 0;
   }
}
