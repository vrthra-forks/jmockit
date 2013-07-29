/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

final class PositionalParameterExpr extends PrimaryExpr
{
   final int index;

   PositionalParameterExpr(int parameterIndex)
   {
      index = parameterIndex;
   }

   public static Expr parse(Tokens tokens)
   {
      if (tokens.hasNext() && '?' == tokens.nextChar()) {
         int paramIndex;

         if (tokens.hasNext()) {
            try {
               paramIndex = Integer.parseInt(tokens.next());
            }
            catch (NumberFormatException ignore) {
               tokens.pushback();
               paramIndex = tokens.nextParameterIndex();
            }
         }
         else {
            paramIndex = tokens.nextParameterIndex();
         }
         
         return new PositionalParameterExpr(paramIndex);
      }

      return null;
   }

   @Override
   public Object evaluate(QueryEval eval)
   {
      return eval.parameters.get(index);
   }
}
