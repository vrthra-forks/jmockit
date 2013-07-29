/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.easymock.samples;

import org.junit.*;

import mockit.*;

public final class BasicClassMock_JMockit_Test
{
   @Injectable private Printer printer;
   @Tested private Document document;

   @Test
   public void testPrintContent()
   {
      new Expectations() {{ printer.print("Hello world"); }};

      document.setContent("Hello world");
      document.print();
   }

   @Test
   public void testPrintEmptyContent()
   {
      new Expectations() {{ printer.print(""); }};

      document.setContent("");
      document.print();
   }
}
