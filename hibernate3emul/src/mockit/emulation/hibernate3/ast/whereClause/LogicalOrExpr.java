/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class LogicalOrExpr extends Expr
{
   final List<Expr> andExprs;

   LogicalOrExpr(List<Expr> andExprs)
   {
      this.andExprs = andExprs;
   }

   public static Expr parse(Tokens tokens)
   {
      Expr andExpr = LogicalAndExpr.parse(tokens);
      List<Expr> andExprs = new LinkedList<Expr>();
      andExprs.add(andExpr);

      while (tokens.hasNext()) {
         if (!"or".equals(tokens.next())) {
            tokens.pushback();
            break;
         }

         andExpr = LogicalAndExpr.parse(tokens);

         if (andExpr == null) {
            throw new QuerySyntaxException(tokens);
         }

         andExprs.add(andExpr);
      }

      return andExprs.size() == 1 ? andExpr : new LogicalOrExpr(andExprs);
   }

   @Override
   public Boolean evaluate(QueryEval eval)
   {
      for (Expr andExpr : andExprs) {
         if ((Boolean) andExpr.evaluate(eval)) {
            return true;
         }
      }

      return false;
   }
}
