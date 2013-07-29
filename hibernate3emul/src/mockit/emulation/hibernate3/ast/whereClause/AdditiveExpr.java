/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.math.*;
import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class AdditiveExpr extends Expr
{
   final Expr[] multiplyExprs;
   final BitSet operators;

   AdditiveExpr(List<Expr> multiplyExprs, BitSet operators)
   {
      this.multiplyExprs = multiplyExprs.toArray(new Expr[multiplyExprs.size()]);
      this.operators = operators;
   }

   public static Expr parse(Tokens tokens)
   {
      int pos = tokens.getPosition();
      Expr firstExpr = MultiplyExpr.parse(tokens);

      if (firstExpr == null) {
         tokens.setPosition(pos);
         return null;
      }

      List<Expr> multiplyExprs = new LinkedList<Expr>();
      multiplyExprs.add(firstExpr);

      BitSet operators = new BitSet();

      for (int i = 0; tokens.hasNext(); i++) {
         char operator = tokens.nextChar();

         if (operator == '+' || operator == '-') {
            Expr nextExpr = MultiplyExpr.parse(tokens);

            if (nextExpr == null) {
               throw new QuerySyntaxException(tokens);
            }

            multiplyExprs.add(nextExpr);

            if (operator == '+') {
               operators.set(i);
            }
         }
         else {
            tokens.pushback();
            break;
         }
      }

      return multiplyExprs.size() == 1 ? firstExpr : new AdditiveExpr(multiplyExprs, operators);
   }

   @Override
   public BigDecimal evaluate(QueryEval eval)
   {
      Object firstValue = multiplyExprs[0].evaluate(eval);
      BigDecimal result = new BigDecimal(firstValue.toString());

      for (int i = 1; i < multiplyExprs.length; i++) {
         Object nextValue = multiplyExprs[i].evaluate(eval);
         BigDecimal operand = new BigDecimal(nextValue.toString());
         result = operators.get(i - 1) ? result.add(operand) : result.subtract(operand);
      }

      return result;
   }
}
