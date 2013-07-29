/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class LogicalAndExprTest
{
   @Test
   public void parseWithOneTerm()
   {
      Tokens tokens = new Tokens("a.code <> 0");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertTrue(expr instanceof EqualityExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseWithThreeTerms()
   {
      Tokens tokens = new Tokens("a.code=1 and b.number < 5 and upper(ab.name) like 'Ab%'");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertFalse(tokens.hasNext());
      assertTrue(expr instanceof LogicalAndExpr);
      LogicalAndExpr andExpr = (LogicalAndExpr) expr;
      assertEquals(3, andExpr.negatedExprs.size());
   }

   @Test
   public void parseWithOneTermContainingANot()
   {
      Tokens tokens = new Tokens("a.code >= 1 and a.code <= 9 and not 5 > b.number");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertFalse(tokens.hasNext());
      assertTrue(expr instanceof LogicalAndExpr);
      LogicalAndExpr andExpr = (LogicalAndExpr) expr;
      assertEquals(3, andExpr.negatedExprs.size());
   }

   @Test
   public void parseSomethingElse()
   {
      Tokens tokens = new Tokens(", test");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertNull(expr);
      assertEquals(-1, tokens.getPosition());
   }

   @Test
   public void parseOrFollowedByRestOfSomeBiggerExpression()
   {
      Tokens tokens = new Tokens("xyz.active )");

      Expr expr = LogicalAndExpr.parse(tokens);

      assertTrue(expr instanceof AccessPathExpr);
      assertEquals(0, tokens.getPosition());
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseWithLastTermMissing()
   {
      Tokens tokens = new Tokens("xyz.active and ||");
      LogicalAndExpr.parse(tokens);
   }

   @Test
   public void evaluateTwoTerms()
   {
      LogicalAndExpr expr = (LogicalAndExpr) LogicalAndExpr.parse(new Tokens("2 < 3 and 2=2"));
      assertTrue(expr.evaluate(new QueryEval()));
   }

   @Test
   public void evaluateThreeTermsWithShortCircuit()
   {
      LogicalAndExpr expr =
         (LogicalAndExpr) LogicalAndExpr.parse(new Tokens("true and false and a > 0"));
      assertFalse(expr.evaluate(new QueryEval()));
   }

   @Test
   public void evaluateAsFalse()
   {
      Tokens tokens = new Tokens("false and 1 != 1.0");
      LogicalAndExpr expr = (LogicalAndExpr) LogicalAndExpr.parse(tokens);
      assertFalse(expr.evaluate(new QueryEval()));
   }
}