/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast;

import java.lang.reflect.*;
import java.util.*;

public final class QueryEval
{
   public final Map<?, ?> parameters;
   public final Map<String, Object> tuple;

   // Just for tests.
   public QueryEval()
   {
      parameters = null;
      tuple = null;
   }

   // Just for tests.
   public QueryEval(Map<?, ?> parameters)
   {
      this.parameters = parameters;
      tuple = null;
   }

   public QueryEval(Map<?, ?> parameters, Map<String, Object> tuple)
   {
      this.parameters = parameters;
      this.tuple = tuple;
   }

   public static Object executeStaticMethod(String name, Object[] args)
   {
      Class<?>[] argClasses = new Class<?>[args.length];

      for (int i = 0; i < args.length; i++) {
         argClasses[i] = args[i].getClass();
      }

      return executeMethod(HQLFunctions.class, name, argClasses, null, args);
   }

   public static Object executeGetter(Object instance, String methodName)
   {
      return executeMethod(instance.getClass(), methodName, new Class<?>[0], instance);
   }

   private static Object executeMethod(
      Class<?> theClass, String name, Class<?>[] paramTypes, Object instance, Object... args)
   {
      try {
         Method getter = theClass.getMethod(name, paramTypes);
         return getter.invoke(instance, args);
      }
      catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e) {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e) {
         Throwable cause = e.getCause();

         if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
         }
         else {
            throw new RuntimeException(cause);
         }
      }
   }
}