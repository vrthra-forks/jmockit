/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.math.*;
import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class MultiplyExpr extends Expr
{
   final Expr[] unaryExprs;
   final BitSet operators;

   MultiplyExpr(List<Expr> unaryExprs, BitSet operators)
   {
      this.unaryExprs = unaryExprs.toArray(new Expr[unaryExprs.size()]);
      this.operators = operators;
   }

   public static Expr parse(Tokens tokens)
   {
      int pos = tokens.getPosition();
      Expr firstExpr = UnaryExpr.parse(tokens);

      if (firstExpr == null) {
         tokens.setPosition(pos);
         return null;
      }

      List<Expr> unaryExprs = new LinkedList<Expr>();
      unaryExprs.add(firstExpr);

      BitSet operators = new BitSet();

      for (int i = 0; tokens.hasNext(); i++) {
         char operator = tokens.nextChar();

         if (operator == '*' || operator == '/') {
            Expr nextExpr = UnaryExpr.parse(tokens);

            if (nextExpr == null) {
               throw new QuerySyntaxException(tokens);
            }

            unaryExprs.add(nextExpr);

            if (operator == '*') {
               operators.set(i);
            }
         }
         else {
            tokens.pushback();
            break;
         }
      }

      return unaryExprs.size() == 1 ? firstExpr : new MultiplyExpr(unaryExprs, operators);
   }

   @Override
   public BigDecimal evaluate(QueryEval eval)
   {
      Object firstValue = unaryExprs[0].evaluate(eval);
      BigDecimal result = new BigDecimal(firstValue.toString());

      for (int i = 1; i < unaryExprs.length; i++) {
         Object nextValue = unaryExprs[i].evaluate(eval);
         BigDecimal operand = new BigDecimal(nextValue.toString());
         result = operators.get(i - 1) ? result.multiply(operand) : result.divide(operand);
      }

      return result;
   }
}
