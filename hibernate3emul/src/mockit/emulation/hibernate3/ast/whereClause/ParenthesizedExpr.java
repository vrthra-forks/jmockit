/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

final class ParenthesizedExpr
{
   static Expr parse(Tokens tokens)
   {
      if ('(' == tokens.nextChar()) {
         Expr subExpr = Expr.parse(tokens);

         if (subExpr == null) {
            // TODO: "(" expressionOrVector | subQuery ")"
         }

         tokens.next(")");
         return subExpr;
      }

      return null;
   }
}
