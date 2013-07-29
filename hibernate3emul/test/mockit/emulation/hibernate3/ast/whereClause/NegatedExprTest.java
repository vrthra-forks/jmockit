/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class NegatedExprTest
{
   @Test
   public void parseWithoutNOT()
   {
      Tokens tokens = new Tokens("2=2");

      Expr expr = NegatedExpr.parse(tokens);

      assertTrue(expr instanceof EqualityExpr);
      assertFalse(tokens.hasNext());
   }

   @Test
   public void parseWithNOT()
   {
      Tokens tokens = new Tokens("not 2=3");

      Expr expr = NegatedExpr.parse(tokens);

      assertTrue(expr instanceof NegatedExpr);
      assertFalse(tokens.hasNext());
      NegatedExpr notExpr = (NegatedExpr) expr;
      assertTrue(notExpr.evaluate(new QueryEval()));
   }
}