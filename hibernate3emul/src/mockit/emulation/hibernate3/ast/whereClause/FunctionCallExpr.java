/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

final class FunctionCallExpr extends PrimaryExpr
{
   final String name;
   final Expr[] arguments;

   FunctionCallExpr(String name, Expr[] arguments)
   {
      this.name = name;
      this.arguments = arguments;
   }

   public static Expr parse(Tokens tokens)
   {
      String name = tokens.next();

      if (tokens.hasNext() && '(' == tokens.nextChar()) {
         Expr arg0 = ConcatenationExpr.parse(tokens);

         // TODO: use exprList
         if (arg0 != null) {
            if (')' == tokens.nextChar()) {
               return new FunctionCallExpr(name, new Expr[] { arg0 });
            }
         }
      }

      return null;
   }

   @Override
   public Object evaluate(QueryEval eval)
   {
      Object[] args = new Object[arguments.length];

      for (int i = 0; i < arguments.length; i++) {
         args[i] = arguments[i].evaluate(eval);
      }

      return eval.executeStaticMethod(name, args);
   }
}
