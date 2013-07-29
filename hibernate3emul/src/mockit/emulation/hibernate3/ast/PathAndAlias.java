/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast;

import java.util.regex.*;

public final class PathAndAlias
{
   private static final Pattern REGEX = Pattern.compile("\\w+(\\.\\w+)*");

   public final String path;
   public final String[] pathElements;
   public final String alias;

   private PathAndAlias(String path, String[] pathElements, String alias)
   {
      this.path = path;
      this.pathElements = pathElements;
      this.alias = alias;
   }

   public PathAndAlias(Tokens tokens)
   {
      path = parsePath(tokens);
      pathElements = path.split("\\.");
      alias = parseAlias(tokens);
   }

   static String parsePath(Tokens tokens)
   {
      String path = tokens.next();

      if (REGEX.matcher(path).matches()) {
         return path;
      }

      throw new QuerySyntaxException(tokens);
   }

   private static String[] parsePathElements(String path)
   {
      String[] pathElements = null;

      if (REGEX.matcher(path).matches()) {
         pathElements = path.split("\\.");
      }

      return pathElements;
   }

   private static String parseAlias(Tokens tokens)
   {
      String alias = tokens.next();

      if ("as".equalsIgnoreCase(alias)) {
         alias = tokens.next();
      }

      if (!Tokens.isIdentifier(alias)) {
         tokens.pushback();
         return null;
      }

      return alias;
   }

   static PathAndAlias parse(Tokens tokens)
   {
      String path = tokens.next();
      String[] pathElements = parsePathElements(path);

      if (pathElements != null) {
         String alias = parseAlias(tokens);
         return new PathAndAlias(path, pathElements, alias);
      }
      else {
         tokens.pushback();
         return null;
      }
   }

   public Object evaluate(Object entity)
   {
      Object result = entity;

      for (int i = 1; i < pathElements.length; i++) {
         String element = pathElements[i];
         String name = "get" + Character.toUpperCase(element.charAt(0)) + element.substring(1);
         result = QueryEval.executeGetter(result, name);
      }

      return result;
   }
}