/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.bypassencapsulation;

import org.junit.*;

import mockit.*;

import powermock.examples.bypassencapsulation.nontest.*;

/**
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/DocumentationExamples/src/test/java/powermock/examples/bypassencapsulation/ReportDaoTest.java">PowerMock version</a>
 */
public final class ReportDao_JMockit_Test
{
   @Tested ReportDao tested;

   @Test
   public void testDeleteReport(@Injectable final Cache cacheMock)
   {
      final String reportName = "reportName";
      final Report report = new Report(reportName);

      new Expectations(tested) {{ // will mock only the "getReportFromTargetName" method
         // Record an expectation for the private method.
         invoke(tested, "getReportFromTargetName", reportName); result = report;
      }};

      tested.deleteReport(reportName);

      new Verifications() {{ cacheMock.invalidateCache(report); }};
   }
}
