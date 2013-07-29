/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.math.*;
import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class RelationalExpr extends Expr
{
   final Expr lhsExpr;
   final Expr rhsExpr;
   final String operator;
   final boolean negated;

   RelationalExpr(Expr lhsExpr, Expr rhsExpr, String operator, boolean negated)
   {
      this.lhsExpr = lhsExpr;
      this.rhsExpr = rhsExpr;
      this.operator = operator;
      this.negated = negated;
   }

   public static Expr parse(Tokens tokens)
   {
      Expr lhsExpr = ConcatenationExpr.parse(tokens);

      if (!tokens.hasNext()) {
         return lhsExpr;
      }

      String operator = tokens.next().toLowerCase();
      boolean negated = false;

      if ("not".equals(operator)) {
         negated = true;
         operator = tokens.next().toLowerCase();
      }

      Expr rhsExpr;

      if (isRelationalOperator(operator)) {
         rhsExpr = !negated ? AdditiveExpr.parse(tokens) : null;
         // TODO: allow repetition
      }
      else if ("in".equals(operator)) {
         rhsExpr = InList.parse(tokens);
      }
      else if ("between".equals(operator)) {
         rhsExpr = BetweenList.parse(tokens);
      }
      else if ("like".equals(operator)) {
         rhsExpr = ConcatenationExpr.parse(tokens);
         // TODO: likeEscape
      }
      else if ("member".equals(operator)) {
         rhsExpr = parseMemberOf(tokens);
      }
      else {
         tokens.pushback();
         return lhsExpr;
      }

      if (rhsExpr == null) {
         throw new QuerySyntaxException(tokens);
      }

      return new RelationalExpr(lhsExpr, rhsExpr, operator, negated);
   }

   private static boolean isRelationalOperator(String operator)
   {
      return
         "<".equals(operator) || "<=".equals(operator) || 
         ">".equals(operator) || ">=".equals(operator);
   }

   private static Expr parseMemberOf(Tokens tokens)
   {
      if (!"of".equalsIgnoreCase(tokens.next())) {
         tokens.pushback();
      }

      return AccessPathExpr.parse(tokens);
   }

   @Override
   public Boolean evaluate(QueryEval eval)
   {
      Object value1 = lhsExpr.evaluate(eval);
      Object value2 = rhsExpr.evaluate(eval);
      boolean result;

      if ("in".equals(operator)) {
         Collection<?> values = (Collection<?>) value2;
         result = values.contains(value1);
      }
      else if ("between".equals(operator)) {
         return evaluateBetweenValues(value1, value2);
      }
      else if ("like".equals(operator)) {
         result = evaluateLikeOperator(value1, value2);
      }
      else if ("member".equals(operator)) {
         return ((Collection<?>) value2).contains(value1);
      }
      else {
         result = evaluateRelationalOperator(value1, value2);
      }

      return negated ? !result : result;
   }

   private Boolean evaluateBetweenValues(Object value1, Object value2)
   {
      Object[] values = (Object[]) value2;
      Comparable<Object> cmpVal = (Comparable<Object>) value1;
      Comparable<Object> cmpVal1 = (Comparable<Object>) values[0];
      Comparable<Object> cmpVal2 = (Comparable<Object>) values[1];

      return cmpVal.compareTo(cmpVal1) >= 0 && cmpVal.compareTo(cmpVal2) <= 0;
   }

   private boolean evaluateRelationalOperator(Object value1, Object value2)
   {
      if (value1 instanceof Number && value2 instanceof Number) {
         value1 = new BigDecimal(value1.toString());
         value2 = new BigDecimal(value2.toString());
      }

      Comparable<Object> cmpVal1 = (Comparable<Object>) value1;
      Comparable<Object> cmpVal2 = (Comparable<Object>) value2;
      int cmp = cmpVal1.compareTo(cmpVal2);
      char operator = this.operator.charAt(0);

      if (this.operator.length() == 1) {
         return operator == '<' ? cmp < 0 : cmp > 0;
      }
      else {
         return operator == '<' ? cmp <= 0 : cmp >= 0;
      }
   }

   private Boolean evaluateLikeOperator(Object value1, Object value2)
   {
      String strVal1 = (String) value1;
      String strVal2 = (String) value2;
      String regex = strVal2.replace("%", ".*");
      return strVal1.matches(regex);
   }
}
