/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class LogicalOrExprTest
{
   @Test
   public void parseWithOneTerm()
   {
      Tokens tokens = new Tokens("a.code <> 0");

      Expr expr = LogicalOrExpr.parse(tokens);

      assertTrue(expr instanceof EqualityExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseWithThreeTerms()
   {
      Tokens tokens = new Tokens("a.code=1 or b.number < 5 or upper(ab.name) like 'Ab%'");

      Expr expr = LogicalOrExpr.parse(tokens);

      assertFalse(tokens.hasNext());
      assertTrue(expr instanceof LogicalOrExpr);
      LogicalOrExpr orExpr = (LogicalOrExpr) expr;
      assertEquals(3, orExpr.andExprs.size());
   }

   @Test
   public void parseWithOneTermContainingAnAnd()
   {
      Tokens tokens = new Tokens("a.code >= 1 and a.code <= 9 or 5 > b.number");

      Expr expr = LogicalOrExpr.parse(tokens);

      assertFalse(tokens.hasNext());
      assertTrue(expr instanceof LogicalOrExpr);
      LogicalOrExpr orExpr = (LogicalOrExpr) expr;
      assertEquals(2, orExpr.andExprs.size());
   }

   @Test
   public void parseSomethingElse()
   {
      Tokens tokens = new Tokens(", test");

      Expr expr = LogicalOrExpr.parse(tokens);

      assertNull(expr);
      assertEquals(-1, tokens.getPosition());
   }

   @Test
   public void parseOrFollowedByRestOfSomeBiggerExpression()
   {
      Tokens tokens = new Tokens("xyz.active )");

      Expr expr = LogicalOrExpr.parse(tokens);

      assertTrue(expr instanceof AccessPathExpr);
      assertEquals(0, tokens.getPosition());
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseWithLastTermMissing()
   {
      Tokens tokens = new Tokens("xyz.active or ||");
      LogicalOrExpr.parse(tokens);
   }

   @Test
   public void evaluateTwoTerms()
   {
      assertEvaluationAsTrue("3 < 2 or 2=2");
   }

   private void assertEvaluationAsTrue(String ql)
   {
      LogicalOrExpr expr = (LogicalOrExpr) LogicalOrExpr.parse(new Tokens(ql));
      assertTrue(expr.evaluate(new QueryEval()));
   }

   @Test
   public void evaluateThreeTermsWithShortCircuit()
   {
      assertEvaluationAsTrue("true or false or a > 0");
   }

   @Test
   public void assertAsFalse()
   {
      LogicalOrExpr expr = (LogicalOrExpr) LogicalOrExpr.parse(new Tokens("false or 1 != 1.0"));
      assertFalse(expr.evaluate(new QueryEval()));
   }
}