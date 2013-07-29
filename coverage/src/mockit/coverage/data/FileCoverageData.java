/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.data;

import java.io.*;
import java.util.*;

import mockit.coverage.dataItems.*;
import mockit.coverage.lines.*;
import mockit.coverage.paths.*;

/**
 * Coverage data gathered for the lines and branches of a single source file.
 */
public final class FileCoverageData implements Serializable
{
   private static final long serialVersionUID = 3508592808457531011L;

   public final PerFileLineCoverage lineCoverageInfo = new PerFileLineCoverage();
   public final PerFilePathCoverage pathCoverageInfo = new PerFilePathCoverage();
   public final PerFileDataCoverage dataCoverageInfo = new PerFileDataCoverage();
   public final PerFileCoverage[] coverageInfos = {lineCoverageInfo, pathCoverageInfo, dataCoverageInfo};

   // Used to track the last time the ".class" file was modified, to decide if merging can be done:
   long lastModified;

   public LineCoverageData addLine(int line) { return lineCoverageInfo.addLine(line); }
   public SortedMap<Integer, LineCoverageData> getLineToLineData() { return lineCoverageInfo.lineToLineData; }

   public void addMethod(MethodCoverageData methodData) { pathCoverageInfo.addMethod(methodData); }
   public Collection<MethodCoverageData> getMethods() { return pathCoverageInfo.firstLineToMethodData.values(); }

   void mergeWithDataFromPreviousTestRun(FileCoverageData previousInfo)
   {
      lineCoverageInfo.mergeInformation(previousInfo.lineCoverageInfo);
      pathCoverageInfo.mergeInformation(previousInfo.pathCoverageInfo);
      dataCoverageInfo.mergeInformation(previousInfo.dataCoverageInfo);
   }

   void reset()
   {
      lineCoverageInfo.reset();
      pathCoverageInfo.reset();
   }
}
