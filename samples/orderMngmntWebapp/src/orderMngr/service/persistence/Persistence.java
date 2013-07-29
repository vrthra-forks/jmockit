/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package orderMngr.service.persistence;

import java.io.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.cfg.*;

public final class Persistence
{
   private static final ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
   private static final SessionFactory sessionFactory;

   static {
      //noinspection CatchGenericClass,OverlyBroadCatchBlock
      try {
         sessionFactory = new Configuration().configure().buildSessionFactory();
      }
      catch (Error e) {
         System.err.println(e);
         throw e;
      }
      catch (RuntimeException e) {
         System.err.println(e);
         throw e;
      }
   }

   // Entity life-cycle methods -------------------------------------------------------------------

   public static <T> T load(Class<T> entityClass, Serializable entityId)
   {
      return (T) session().load(entityClass, entityId);
   }

   public static boolean exists(Class<?> entityClass, Serializable entityId)
   {
      return session().get(entityClass, entityId) != null;
   }

   public static void unload(Object entity)
   {
      session().evict(entity);
   }

   public static void persist(Object entityData)
   {
      session().save(entityData);
   }

   public static void delete(Object entity)
   {
      session().delete(entity);
   }

   private static Session session()
   {
      Session session = currentSession.get();

      if (session == null) {
         session = sessionFactory.openSession();
         currentSession.set(session);
      }

      return session;
   }

   // Query methods -------------------------------------------------------------------------------

   public static <E> List<E> find(String ql, Object... args)
   {
      Query query = newQuery(ql, args);
      return query.list();
   }

   private static Query newQuery(String ql, Object... args)
   {
      Query query = session().createQuery(ql);

      for (int i = 0; i < args.length; i++) {
         query.setParameter(i, args[i]);
      }

      return query;
   }

   public static boolean exists(String ql, Object... args)
   {
      Query query = newQuery(ql, args);
      query.setMaxResults(1);
      return query.iterate().hasNext();
   }

   // Methods related to the Unit of Work and the Transaction -------------------------------------

   public static void persistPendingChanges()
   {
      session().flush();
      session().beginTransaction();
   }

   public static void clearUnitOfWork()
   {
      session().clear();
   }

   public static void closeUnitOfWork()
   {
      Session session = currentSession.get();

      if (session != null) {
         session.close();
         currentSession.set(null);
      }
   }

   public static void beginTransaction()
   {
      session().beginTransaction();
   }

   public static void rollbackTransaction()
   {
      session().getTransaction().rollback();
   }

   // Miscelaneous methods ------------------------------------------------------------------------

   public static Serializable entityId(Object entity)
   {
      return session().getIdentifier(entity);
   }
}
