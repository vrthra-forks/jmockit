/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class ConstantExprTest
{
   @Test
   public void parseNUM_INT()
   {
      assertParseAndEvaluateConstant("4", 4);
   }

   @Test
   public void parseNUM_LONG()
   {
      assertParseAndEvaluateConstant("9876543210", 9876543210L);
   }

   @Test
   public void parseNUM_DOUBLE()
   {
      assertParseAndEvaluateConstant("987654321098.01", 987654321098.01);
   }

   @Test
   public void parseQUOTED_STRING()
   {
      assertParseAndEvaluateConstant("'Test'", "Test");
   }

   @Test
   public void parseNULL()
   {
      assertParseAndEvaluateConstant("null", null);
   }

   @Test
   public void parseTRUE()
   {
      assertParseAndEvaluateConstant("true", true);
   }

   @Test
   public void parseFALSE()
   {
      assertParseAndEvaluateConstant("false", false);
   }

   @Test
   public void parseEMPTY()
   {
      assertParseAndEvaluateConstant("empty", Collections.EMPTY_SET);
   }

   private void assertParseAndEvaluateConstant(String token, Object value)
   {
      ConstantExpr expr = ConstantExpr.parse(new Tokens(token));
      assertEquals(value, expr.value);
      assertEquals(value, expr.evaluate(new QueryEval()));
   }

   public void parseNotAKnownConstant()
   {
      assertNull(ConstantExpr.parse(new Tokens("jsdf")));
   }
}
