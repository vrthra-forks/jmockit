/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

abstract class PrimaryExpr extends Expr
{
   // TODO: identPrimary "." "class"
   public static Expr parse(Tokens tokens)
   {
      int startingPos = tokens.getPosition();

      Expr expr = PositionalParameterExpr.parse(tokens);
      if (expr != null) return expr;
      tokens.setPosition(startingPos);

      expr = NamedParameterExpr.parse(tokens);
      if (expr != null) return expr;
      tokens.setPosition(startingPos);

      expr = ConstantExpr.parse(tokens);
      if (expr != null) return expr;
      tokens.setPosition(startingPos);

      expr = FunctionCallExpr.parse(tokens);
      if (expr != null) return expr;
      tokens.setPosition(startingPos);

      expr = AccessPathExpr.parse(tokens);
      if (expr != null) return expr;
      tokens.setPosition(startingPos);

      expr = ParenthesizedExpr.parse(tokens);
      if (expr != null) return expr;
      tokens.setPosition(startingPos);

      return null;
   }
}
