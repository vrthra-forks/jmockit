/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import mockit.emulation.hibernate3.ast.*;

final class AccessPathExpr extends PrimaryExpr
{
   final String[] accessElements;

   AccessPathExpr(String accessPath)
   {
      accessElements = accessPath.split("\\.");
   }

   public static Expr parse(Tokens tokens)
   {
      if (tokens.hasNext()) {
         String accessPath = tokens.next();

         if (accessPath.matches("\\w+(\\.\\w+)*")) {
            return new AccessPathExpr(accessPath);
         }
      }

      return null;
   }

   @Override
   public Object evaluate(QueryEval eval)
   {
      Object result = eval.tuple.get(accessElements[0]);

      for (int i = 1; i < accessElements.length; i++) {
         String accessElement = accessElements[i];
         String name =
            "get" + Character.toUpperCase(accessElement.charAt(0)) + accessElement.substring(1);
         result = QueryEval.executeGetter(result, name);
      }

      return result;
   }
}
