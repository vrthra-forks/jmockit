/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting;

import mockit.coverage.data.*;

public final class BasicCoverageReport extends CoverageReport
{
   public BasicCoverageReport(String outputDir, String[] sourceDirs, CoverageData coverageData)
   {
      super(outputDir, sourceDirs, coverageData, false);
   }
}
