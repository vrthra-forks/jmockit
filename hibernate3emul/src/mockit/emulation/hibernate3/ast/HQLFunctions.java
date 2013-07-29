/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast;

final class HQLFunctions
{
   public static String lower(String s)
   {
      return s.toLowerCase();
   }
   
   public static int abs(Integer n)
   {
      return Math.abs(n);
   }
}