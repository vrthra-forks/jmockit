/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.fromClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

final class FromClassOrOuterQueryPath
{
   final boolean isFQName;
   final String entityClassName;
   final String alias;
   final List<Object> result;
   FromJoin join;

   FromClassOrOuterQueryPath(String path, String alias)
   {
      isFQName = path.contains(".");
      entityClassName = path;
      this.alias = alias;
      result = new ArrayList<Object>();
   }

   static FromClassOrOuterQueryPath parse(Tokens tokens)
   {
      if (tokens.hasNext()) {
         PathAndAlias path = new PathAndAlias(tokens);
         return new FromClassOrOuterQueryPath(path.path, path.alias);
      }

      throw new QuerySyntaxException(tokens);
   }

   int depth()
   {
      return 1 + (join == null ? 0 : join.depth());
   }

   int tupleCount()
   {
      if (join == null) {
         return result.size();
      }
      else {
         int count = 0;

         for (Object entity : result) {
            count += join.tupleCount(entity);
         }

         return count;
      }
   }

   void matches(Collection<?> entities)
   {
      for (Object entity : entities) {
         Class<?> entityClass = entity.getClass();
         String className = isFQName ? entityClass.getName() : entityClass.getSimpleName();

         if (className.equals(entityClassName) && (join == null || join.matches(entity, alias))) {
            result.add(entity);
         }
      }
   }

   public int columnIndex(String alias)
   {
      if (this.alias.equals(alias)) {
         return 0;
      }
      else if (join != null) {
         return join.columnIndex(alias);
      }
      else {
         return -1;
      }
   }

   void getAliases(Map<String, Object> aliasToValue)
   {
      aliasToValue.put(alias, null);

      FromJoin join = this.join;

      while (join != null) {
         aliasToValue.put(join.pathAndAlias.alias, null);
         join = join.nextJoin;
      }
   }
}