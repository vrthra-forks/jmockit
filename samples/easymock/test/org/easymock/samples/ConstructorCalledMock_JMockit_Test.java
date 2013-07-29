/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.easymock.samples;

import java.math.*;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public final class ConstructorCalledMock_JMockit_Test
{
   private final TaxCalculator tc = new TaxCalculator(new BigDecimal("5"), new BigDecimal("15")) {
      @Override
      protected BigDecimal rate() { return null; }
   };

   @Test
   public void testTax()
   {
      new Expectations(tc) {{ tc.rate(); result = new BigDecimal("0.20"); }};

      assertEquals(new BigDecimal("4.00"), tc.tax());
   }

   @Test
   public void testTax_ZeroRate()
   {
      new Expectations(tc) {{ tc.rate(); result = BigDecimal.ZERO; }};

      assertEquals(BigDecimal.ZERO, tc.tax());
   }
}
