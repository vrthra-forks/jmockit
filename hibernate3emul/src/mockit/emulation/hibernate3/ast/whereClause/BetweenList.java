/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

final class BetweenList extends Expr
{
   final Expr expr1;
   final Expr expr2;

   BetweenList(Expr expr1, Expr expr2)
   {
      this.expr1 = expr1;
      this.expr2 = expr2;
   }

   public static Expr parse(Tokens tokens)
   {
      Expr expr1 = ConcatenationExpr.parse(tokens);
      tokens.next("and");
      Expr expr2 = ConcatenationExpr.parse(tokens);

      return new BetweenList(expr1, expr2);
   }

   @Override
   public Object[] evaluate(QueryEval eval)
   {
      Object result1 = expr1.evaluate(eval);
      Object result2 = expr2.evaluate(eval);
      return new Object[] { result1, result2 };
   }
}
