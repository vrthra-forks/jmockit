/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.fromClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class FromJoin
{
   enum JoinType { Inner, Full, LeftOuter, RightOuter }

   final JoinType type;
   final PathAndAlias pathAndAlias;
   final Map<Object, List<Object>> result;
   FromJoin nextJoin;

   FromJoin(JoinType type, PathAndAlias pathAndAlias)
   {
      this.type = type;
      this.pathAndAlias = pathAndAlias;
      result = new HashMap<Object, List<Object>>();
   }

   static FromJoin parse(Tokens tokens)
   {
      String token = tokens.next().toLowerCase();
      JoinType joinType;

      if ("join".equals(token)) {
         joinType = JoinType.Inner;
      }
      else if ("inner".equals(token)) {
         tokens.next("join");
         joinType = JoinType.Inner;
      }
      else if ("full".equals(token)) {
         tokens.next("join");
         joinType = JoinType.Full;
      }
      else if ("left".equals(token)) {
         parseOuterJoin(tokens);
         joinType = JoinType.LeftOuter;
      }
      else if ("right".equals(token)) {
         parseOuterJoin(tokens);
         joinType = JoinType.RightOuter;
      }
      else {
         tokens.pushback();
         return null;
      }

      token = tokens.next();

      if (!"fetch".equalsIgnoreCase(token)) {
         tokens.pushback();
      }

      PathAndAlias path = new PathAndAlias(tokens);

      return new FromJoin(joinType, path);
   }

   private static void parseOuterJoin(Tokens tokens)
   {
      String token = tokens.next().toLowerCase();

      if ("outer".equals(token)) {
         tokens.next("join");
      }
      else if (!"join".equals(token)) {
         throw new QuerySyntaxException(tokens);
      }
   }

   int depth()
   {
      return 1 + (nextJoin == null ? 0 : nextJoin.depth());
   }

   public int tupleCount(Object entity)
   {
      List<Object> childValues = result.get(entity);

      if (nextJoin == null) {
         return childValues.size();
      }
      else {
         int count = 0;

         for (Object childValue : childValues) {
            count += nextJoin.tupleCount(childValue);
         }

         return count;
      }
   }

   boolean matches(Object entity, String entityAlias)
   {
      String initialElement = pathAndAlias.pathElements[0];

      if (!entityAlias.equals(initialElement)) {
         throw new RuntimeException(
            "Unknown path element \"" + initialElement + "\", expected " + entityAlias);
      }

      Object joinResult = pathAndAlias.evaluate(entity);
      if (joinResult == null) return false;

      Collection<?> joinResultCollection;

      if (joinResult instanceof Collection) {
         joinResultCollection = (Collection<?>) joinResult;

         if (joinResultCollection.isEmpty()) {
            return false;
         }
      }
      else {
         joinResultCollection = null;
      }

      if (nextJoin == null) {
         addResult(entity, joinResult);
         return true;
      }

      String pathAlias = pathAndAlias.alias;
      boolean matchesNextJoin;

      if (joinResultCollection != null) {
         matchesNextJoin = true;

         for (Object resultElement : joinResultCollection) {
            if (!nextJoin.matches(resultElement, pathAlias)) {
               matchesNextJoin = false;
               break;
            }
         }
      }
      else {
         matchesNextJoin = nextJoin.matches(joinResult, pathAlias);
      }

      if (matchesNextJoin) {
         addResult(entity, joinResult);
      }

      return matchesNextJoin;
   }

   private void addResult(Object parentEntity, Object joinResult)
   {
      List<Object> childrenValues = new LinkedList<Object>();

      if (joinResult instanceof Collection) {
         childrenValues.addAll((Collection<?>) joinResult);
      }
      else {
         childrenValues.add(joinResult);
      }

      result.put(parentEntity, childrenValues);
   }

   int columnIndex(String alias)
   {
      if (alias.equals(pathAndAlias.alias)) {
         return 1;
      }
      else if (nextJoin != null) {
         return 1 + nextJoin.columnIndex(alias);
      }
      else {
         return -1;
      }
   }
}