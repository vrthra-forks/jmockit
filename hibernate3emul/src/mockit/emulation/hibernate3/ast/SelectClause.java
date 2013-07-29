/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast;

import java.util.*;

final class SelectClause
{
   final boolean distinct;
   final String constructorName;
   final List<PathAndAlias> selectedProperties;

   private SelectClause(
      boolean distinct, String constructorName, List<PathAndAlias> selectedProperties)
   {
      this.distinct = distinct;
      this.selectedProperties = selectedProperties;
      this.constructorName = constructorName;
   }

   static SelectClause parse(Tokens tokens)
   {
      if (!"select".equalsIgnoreCase(tokens.next())) {
         tokens.pushback();
         return null;
      }

      String token = tokens.next();
      boolean distinct = "distinct".equalsIgnoreCase(token);

      if (!distinct) {
         tokens.pushback();
      }

      token = tokens.next();
      String constructorName;
      List<PathAndAlias> seletedProperties;

      if ("new".equalsIgnoreCase(token)) {
         constructorName = PathAndAlias.parsePath(tokens);
         tokens.next("(");
         seletedProperties = parseSelectedPropertiesList(tokens);
         tokens.next(")");
      }
      else {
         tokens.pushback();
         constructorName = null;
         seletedProperties = parseSelectedPropertiesList(tokens);
      }

      return new SelectClause(distinct, constructorName, seletedProperties);
   }

   private static List<PathAndAlias> parseSelectedPropertiesList(Tokens tokens)
   {
      PathAndAlias pathAndAlias = PathAndAlias.parse(tokens);
      if (pathAndAlias == null) return null;

      List<PathAndAlias> seletedProperties = new LinkedList<PathAndAlias>();
      seletedProperties.add(pathAndAlias);

      while (true) {
         char c = tokens.nextChar();

         if (c != ',') {
            tokens.pushback();
            break;
         }

         pathAndAlias = PathAndAlias.parse(tokens);
         seletedProperties.add(pathAndAlias);
      }

      return seletedProperties;
   }
}
