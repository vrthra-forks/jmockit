/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class ConcatenationExpr extends Expr
{
   final Expr[] additiveExprs;

   ConcatenationExpr(List<Expr> additiveExprs)
   {
      this.additiveExprs = additiveExprs.toArray(new Expr[additiveExprs.size()]);
   }

   public static Expr parse(Tokens tokens)
   {
      int pos = tokens.getPosition();
      Expr firstExpr = AdditiveExpr.parse(tokens);

      if (firstExpr == null) {
         tokens.setPosition(pos);
         return null;
      }

      List<Expr> additiveExprs = new LinkedList<Expr>();
      additiveExprs.add(firstExpr);

      while (tokens.hasNext()) {
         if ("||".equals(tokens.next())) {
            Expr nextExpr = AdditiveExpr.parse(tokens);

            if (nextExpr == null) {
               throw new QuerySyntaxException(tokens);
            }

            additiveExprs.add(nextExpr);
         }
         else {
            tokens.pushback();
            break;
         }
      }

      return additiveExprs.size() == 1 ? firstExpr : new ConcatenationExpr(additiveExprs);
   }

   @Override
   public String evaluate(QueryEval eval)
   {
      StringBuilder result = new StringBuilder();

      for (Expr expr : additiveExprs) {
         result.append(expr.evaluate(eval));
      }

      return result.toString();
   }
}