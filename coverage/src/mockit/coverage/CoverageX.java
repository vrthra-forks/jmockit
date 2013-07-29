/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage;

public final class CoverageX
{
  public int _N;
  public int _D;
  public double _R;

  public CoverageX(int coveredCount, int totalCount)
  {
    if (totalCount <= 0) {
      _N = 0;
      _D = 0;
      _R = -1;
    } else {
      _N = coveredCount;
      _D = totalCount;
      _R = (100.0 * coveredCount / totalCount);
    }
  }
  public String toString() {
    return "(" + _N + "/" + _D + ") = " + _R;
  }
}
