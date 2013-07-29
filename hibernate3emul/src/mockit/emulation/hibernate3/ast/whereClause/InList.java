/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.whereClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class InList extends Expr
{
   final List<Expr> elements;

   InList(List<Expr> elements)
   {
      this.elements = elements;
   }

   // TODO: collectionExpr | path | (subQuery)
   public static Expr parse(Tokens tokens)
   {
      tokens.next("(");

      List<Expr> elements = new LinkedList<Expr>();
      Expr element = Expr.parse(tokens);

      while (element != null) {
         elements.add(element);

         char nextChar = tokens.nextChar();

         if (',' == nextChar) {
            element = Expr.parse(tokens);
         }
         else if (')' == nextChar) {
            break;
         }
         else {
            throw new QuerySyntaxException(tokens);
         }
      }

      if (element == null) {
         throw new QuerySyntaxException(tokens);
      }

      return new InList(elements);
   }

   @Override
   public List<Object> evaluate(QueryEval eval)
   {
      List<Object> values = new ArrayList<Object>(elements.size());

      for (Expr element : elements) {
         values.add(element.evaluate(eval));
      }

      return values;
   }
}
