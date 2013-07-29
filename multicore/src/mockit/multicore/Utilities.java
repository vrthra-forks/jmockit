/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.lang.reflect.*;

@SuppressWarnings("unchecked")
final class Utilities
{
   static <T> T getField(Class<?> ownerClass, Object owner, String fieldName)
   {
      Field f = getField(ownerClass, fieldName);
      try { return (T) f.get(owner); } catch (IllegalAccessException e) { throw new RuntimeException(e); }
   }

   private static Field getField(Class<?> ownerClass, String fieldName)
   {
      Field f;
      try { f = ownerClass.getDeclaredField(fieldName); }
      catch (NoSuchFieldException e) { throw new RuntimeException(e); }
      f.setAccessible(true);
      return f;
   }
}
