/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class ConcatenationExprTest
{
   @Test
   public void parseSingleValue()
   {
      Tokens tokens = new Tokens("x.abc");

      Expr expr = ConcatenationExpr.parse(tokens);

      assertTrue(expr instanceof AccessPathExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseSingleAdditiveExpr()
   {
      Tokens tokens = new Tokens("x.abc + 7");

      Expr expr = ConcatenationExpr.parse(tokens);

      assertTrue(expr instanceof AdditiveExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseConcatenation()
   {
      Tokens tokens = new Tokens("x.abc || 'constant'");

      Expr expr = ConcatenationExpr.parse(tokens);

      assertTrue(expr instanceof ConcatenationExpr);
      assertFalse(tokens.hasNext());
      ConcatenationExpr concatExpr = (ConcatenationExpr) expr;
      assertEquals(2, concatExpr.additiveExprs.length);
      assertTrue(concatExpr.additiveExprs[0] instanceof AccessPathExpr);
      assertTrue(concatExpr.additiveExprs[1] instanceof ConstantExpr);
   }

   @Test
   public void parseConcatenationOfThreeValues()
   {
      Tokens tokens = new Tokens("x.abc || fh(0)||'test'");

      Expr expr = ConcatenationExpr.parse(tokens);

      assertTrue(expr instanceof ConcatenationExpr);
      assertFalse(tokens.hasNext());
      ConcatenationExpr concatExpr = (ConcatenationExpr) expr;
      assertEquals(3, concatExpr.additiveExprs.length);
      assertTrue(concatExpr.additiveExprs[0] instanceof AccessPathExpr);
      assertTrue(concatExpr.additiveExprs[1] instanceof FunctionCallExpr);
      assertTrue(concatExpr.additiveExprs[2] instanceof ConstantExpr);
   }

   @Test(expected = QuerySyntaxException.class)
   public void parseWithSyntaxError()
   {
      ConcatenationExpr.parse(new Tokens("a || ||"));
   }

   @Test
   public void evaluate()
   {
      ConcatenationExpr expr =
         (ConcatenationExpr) ConcatenationExpr.parse(new Tokens("'XYZ' || -345"));

      String result = expr.evaluate(new QueryEval());

      assertEquals("XYZ-345", result);
   }
}