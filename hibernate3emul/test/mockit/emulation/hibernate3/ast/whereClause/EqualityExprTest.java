/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import static java.util.Arrays.*;
import java.util.*;

import mockit.emulation.hibernate3.ast.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class EqualityExprTest
{
   private QueryEval eval = new QueryEval();
   private Map<Object, Object> parameters = new HashMap<Object, Object>();

   @Test
   public void parseEQ()
   {
      EqualityExpr eqExpr = parseEquality("a.b = 46");

      assertTrue(eqExpr.lhsExpr instanceof AccessPathExpr);
      assertTrue(eqExpr.rhsExpr instanceof ConstantExpr);
      assertEquals("=", eqExpr.operator);
   }

   private EqualityExpr parseEquality(String ql)
   {
      Tokens tokens = new Tokens(ql);

      Expr expr = EqualityExpr.parse(tokens);

      assertTrue(expr instanceof EqualityExpr);
      assertFalse(tokens.hasNext());
      return (EqualityExpr) expr;
   }

   @Test
   public void parseNE()
   {
      EqualityExpr eqExpr = parseEquality("5.09 != g.jk");

      assertTrue(eqExpr.lhsExpr instanceof ConstantExpr);
      assertTrue(eqExpr.rhsExpr instanceof AccessPathExpr);
      assertEquals("!=", eqExpr.operator);
   }

   @Test
   public void parseSQL_NE()
   {
      EqualityExpr eqExpr = parseEquality("0 - 1 <> 4 / 3");

      assertTrue(eqExpr.lhsExpr instanceof AdditiveExpr);
      assertTrue(eqExpr.rhsExpr instanceof MultiplyExpr);
      assertEquals("<>", eqExpr.operator);
   }

   @Test
   public void parseIS()
   {
      EqualityExpr eqExpr = parseEquality("xy is null");

      assertTrue(eqExpr.lhsExpr instanceof AccessPathExpr);
      assertTrue(eqExpr.rhsExpr instanceof ConstantExpr);
      assertEquals("is", eqExpr.operator);
   }

   @Test
   public void parseISNOT()
   {
      EqualityExpr eqExpr = parseEquality("xy || abc IS NOT null");

      assertTrue(eqExpr.lhsExpr instanceof ConcatenationExpr);
      assertTrue(eqExpr.rhsExpr instanceof ConstantExpr);
      assertEquals("isnot", eqExpr.operator);
   }

   @Test
   public void parseRelationalExpr()
   {
      Tokens tokens = new Tokens("1 < 2");
      Expr expr = EqualityExpr.parse(tokens);

      assertTrue(expr instanceof RelationalExpr);
      assertFalse(tokens.hasNext());
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseWithSyntaxError()
   {
      EqualityExpr.parse(new Tokens("1 = !"));
   }

   @Test
   public void parseRelationalExprFollowedBySomething()
   {
      Tokens tokens = new Tokens("1 >= -6 and");
      Expr expr = EqualityExpr.parse(tokens);

      assertTrue(expr instanceof RelationalExpr);
      assertEquals(2, tokens.getPosition());
   }

   @Test
   public void evaluateEQ()
   {
      assertEvaluateAsTrue("46 = 46");
   }

   private void assertEvaluateAsTrue(String ql)
   {
      EqualityExpr eqExpr = parseEquality(ql);
      assertTrue(eqExpr.evaluate(eval));
   }

   @Test
   public void evaluateNE()
   {
      assertEvaluateAsTrue("'Abc' != 'XYZ'");
   }

   @Test
   public void evaluateSQL_NE()
   {
      assertEvaluateAsTrue("-4 <> 4.0");
   }

   @Test
   public void evaluateIS_NULL()
   {
      eval = new QueryEval(parameters);
      assertEvaluateAsTrue("? is null");
   }

   @Test
   public void evaluateIS_EMPTY()
   {
      parameters.put(0, Collections.EMPTY_LIST);
      eval = new QueryEval(parameters);
      assertEvaluateAsTrue("? is empty");
   }

   @Test(expected = IllegalStateException.class)
   public void evaluateISInvalidValue()
   {
      parameters.put(0, 5);
      eval = new QueryEval(parameters);
      assertEvaluateAsTrue("? is true");
   }

   @Test
   public void evaluateISNOT_NULL()
   {
      parameters.put(2, 5);
      eval = new QueryEval(parameters);
      assertEvaluateAsTrue("?2 is not null");
   }

   @Test
   public void evaluateISNOT_EMPTY()
   {
      parameters.put(2, asList(5));
      eval = new QueryEval(parameters);
      assertEvaluateAsTrue("?2 is not empty");
   }

   @Test(expected = IllegalStateException.class)
   public void evaluateISNOTInvalidValue()
   {
      parameters.put(2, 5);
      eval = new QueryEval(parameters);
      assertEvaluateAsTrue("?2 is not 32");
   }
}