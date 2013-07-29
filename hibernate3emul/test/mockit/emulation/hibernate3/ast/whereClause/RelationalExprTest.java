/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class RelationalExprTest
{
   private QueryEval eval = new QueryEval();

   @Test
   public void parseLT()
   {
      assertParseAndEvaluate("1 < 2");
   }

   private void assertParseAndEvaluate(String ql)
   {
      Tokens tokens = new Tokens(ql);
      Expr expr = RelationalExpr.parse(tokens);

      assertTrue(expr instanceof RelationalExpr);
      assertFalse(tokens.hasNext());
      assertTrue(((RelationalExpr) expr).evaluate(eval));
   }

   @Test
   public void parseGT()
   {
      assertParseAndEvaluate("42 > 2.7");
   }

   @Test
   public void parseLE()
   {
      assertParseAndEvaluate("-5 <= 70 - 20");
   }

   @Test
   public void parseGE()
   {
      assertParseAndEvaluate("42 * 2 + 3 >= 2.7 / 3");
   }

   @Test
   public void parseIN()
   {
      assertParseAndEvaluate("6 in (1, 42, 6, 0)");
   }

   @Test
   public void parseNOT_IN()
   {
      assertParseAndEvaluate("7 not in (1, 42, 6, 0)");
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseINMissingClosingComma()
   {
      assertParseAndEvaluate("6 in (1");
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseINWithSyntaxError()
   {
      assertParseAndEvaluate("6 in (4 [)");
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseINWithEmptyList()
   {
      assertParseAndEvaluate("6 in ()");
   }

   @Test
   public void parseMEMBER()
   {
      Map<String, Object> tuple = new HashMap<String, Object>();
      tuple.put("a", new EntityA());
      eval = new QueryEval(null, tuple);
      assertParseAndEvaluate("6 member a.numbers");
   }

   public static class EntityA
   {
      public Collection<Integer> getNumbers() { return Arrays.asList(5, 6, 0); }
   }

   @Test
   public void parseLIKEPrefix()
   {
      assertParseAndEvaluate("'Abcdef123' like 'Abc%'");
   }

   @Test
   public void parseLIKESuffix()
   {
      assertParseAndEvaluate("'Abcdef123' like '%123'");
   }

   @Test
   public void parseLIKEAnyFragment()
   {
      assertParseAndEvaluate("'Abcdef123' like '%def%'");
   }

   @Test
   public void parseBETWEEN()
   {
      assertParseAndEvaluate("3 between 1 and 5");
   }

   @Test
   public void parseJustAConcatenationExpr()
   {
      Tokens tokens = new Tokens("12");
      Expr expr = RelationalExpr.parse(tokens);
      assertNotNull(expr);
      assertFalse(tokens.hasNext());
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseWithSyntaxError()
   {
      RelationalExpr.parse(new Tokens("5 < "));
   }
}