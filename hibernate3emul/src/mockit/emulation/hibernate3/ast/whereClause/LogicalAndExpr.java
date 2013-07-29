/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class LogicalAndExpr extends Expr
{
   final List<Expr> negatedExprs;

   LogicalAndExpr(List<Expr> negatedExprs)
   {
      this.negatedExprs = negatedExprs;
   }

   public static Expr parse(Tokens tokens)
   {
      Expr negatedExpr = NegatedExpr.parse(tokens);
      List<Expr> negatedExprs = new LinkedList<Expr>();
      negatedExprs.add(negatedExpr);

      while (tokens.hasNext()) {
         if (!"and".equalsIgnoreCase(tokens.next())) {
            tokens.pushback();
            break;
         }

         negatedExpr = NegatedExpr.parse(tokens);

         if (negatedExpr == null) {
            throw new QuerySyntaxException(tokens);
         }

         negatedExprs.add(negatedExpr);
      }
      
      return negatedExprs.size() == 1 ? negatedExpr : new LogicalAndExpr(negatedExprs);
   }

   @Override
   public Boolean evaluate(QueryEval eval)
   {
      for (Expr expr : negatedExprs) {
         Boolean value = (Boolean) expr.evaluate(eval);

         if (value != null && !value) {
            return false;
         }
      }

      return true;
   }
}