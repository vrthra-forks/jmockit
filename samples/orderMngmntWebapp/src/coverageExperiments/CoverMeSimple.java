package coverageExperiments;

public class CoverMeSimple
{
   // This is an empty path, but still a path.
   public CoverMeSimple() {}

   public int simple(int x, int y)
   {
      int z = x;

      if (y > x) {
         z = y;
      }

      z *= 2;

      return z;
   }
}