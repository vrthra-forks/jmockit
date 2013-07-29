/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.dataItems;

import java.util.*;

import mockit.internal.state.*;

public final class StaticFieldData extends FieldData
{
   private static final long serialVersionUID = -6596622341651601060L;

   private final transient Map<Integer, Boolean> testIdsToAssignments = new HashMap<Integer, Boolean>();

   void registerAssignment()
   {
      int testId = TestRun.getTestId();
      testIdsToAssignments.put(testId, Boolean.TRUE);
      writeCount++;
   }

   void registerRead()
   {
      int testId = TestRun.getTestId();
      testIdsToAssignments.put(testId, null);
      readCount++;
   }

   @Override
   void markAsCoveredIfNoUnreadValuesAreLeft()
   {
      for (Boolean withUnreadValue : testIdsToAssignments.values()) {
         if (withUnreadValue == null) {
            covered = true;
            break;
         }
      }
   }
}