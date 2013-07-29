/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.math.*;

import mockit.emulation.hibernate3.ast.*;

final class UnaryExpr extends Expr
{
   final Expr rhsExpr;

   UnaryExpr(Expr expr)
   {
      rhsExpr = expr;
   }

   // TODO: caseExpr, quantifiedExpr
   public static Expr parse(Tokens tokens)
   {
      if (tokens.hasNext()) {
         String token = tokens.next();
         char sign = token.charAt(0);

         if (sign == '-') {
            tokens.pushback();
            Expr rhsExpr = PrimaryExpr.parse(tokens);
            return rhsExpr instanceof ConstantExpr ? rhsExpr : new UnaryExpr(rhsExpr);
         }
         else if (sign == '+') {
            return PrimaryExpr.parse(tokens);
         }
         else {
            tokens.pushback();
            return PrimaryExpr.parse(tokens);
         }
      }

      return null;
   }

   @Override
   public Object evaluate(QueryEval eval)
   {
      Object value = rhsExpr.evaluate(eval);

      Number numericValue = (Number) value;

      if (numericValue instanceof BigInteger) {
         numericValue = ((BigInteger) numericValue).negate();
      }
      else if (numericValue instanceof BigDecimal) {
         numericValue = ((BigDecimal) numericValue).negate();
      }
      else if (numericValue instanceof Float) {
         numericValue = -numericValue.floatValue();
      }
      else if (numericValue instanceof Double) {
         numericValue = -numericValue.doubleValue();
      }
      else if (numericValue instanceof Integer) {
         numericValue = -numericValue.intValue();
      }
      else if (numericValue instanceof Short) {
         numericValue = -numericValue.shortValue();
      }
      else if (numericValue instanceof Byte) {
         numericValue = -numericValue.byteValue();
      }
      else {
         numericValue = -numericValue.longValue();
      }

      return numericValue;
   }
}
