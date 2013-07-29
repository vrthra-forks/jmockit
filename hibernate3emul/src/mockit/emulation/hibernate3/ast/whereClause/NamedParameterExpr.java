/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

final class NamedParameterExpr extends PrimaryExpr
{
   final String name;

   NamedParameterExpr(String paramName)
   {
      name = paramName;
   }

   public static Expr parse(Tokens tokens)
   {
      if (tokens.hasNext() && ':' == tokens.nextChar()) {
         String paramName = tokens.next();
         return new NamedParameterExpr(paramName);
      }

      return null;
   }

   @Override
   public Object evaluate(QueryEval eval)
   {
      return eval.parameters.get(name);
   }
}
