/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class ConstantExpr extends PrimaryExpr
{
   final Object value;

   ConstantExpr(Object value)
   {
      this.value = value;
   }

   @SuppressWarnings({"UnusedCatchParameter"})
   public static ConstantExpr parse(Tokens tokens)
   {
      String strValue = tokens.next();
      Object value;

      try {
         value = Integer.valueOf(strValue);
      }
      catch (NumberFormatException ignore1) {
         try {
            value = Long.valueOf(strValue);
         }
         catch (NumberFormatException ignore2) {
            try {
               value = Double.valueOf(strValue);
            }
            catch (NumberFormatException ignore3) {
               if ("true".equalsIgnoreCase(strValue)) {
                  value = true;
               }
               else if ("false".equalsIgnoreCase(strValue)) {
                  value = false;
               }
               else if ("null".equalsIgnoreCase(strValue)) {
                  value = null;
               }
               else if ("empty".equalsIgnoreCase(strValue)) {
                  value = Collections.EMPTY_SET;
               }
               else if (
                  strValue.charAt(0) == '\'' && strValue.charAt(strValue.length() - 1) == '\'') {
                  value = strValue.substring(1, strValue.length() - 1);
               }
               else {
                  return null;
               }
            }
         }
      }

      return new ConstantExpr(value);
   }

   @Override
   public Object evaluate(QueryEval eval)
   {
      return value;
   }
}
