/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3;

import java.io.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.classic.Session;
import org.hibernate.type.*;

final class ClassicSessionEmul extends SessionEmul implements Session
{
   ClassicSessionEmul(SessionFactory sessionFactory)
   {
      super(sessionFactory, null);
   }

   public Object saveOrUpdateCopy(Object object)
   {
      return null;
   }

   public Object saveOrUpdateCopy(Object object, Serializable id)
   {
      return null;
   }

   public Object saveOrUpdateCopy(String entityName, Object object)
   {
      return null;
   }

   public Object saveOrUpdateCopy(String entityName, Object object, Serializable id)
   {
      return null;
   }

   public List<?> find(String query)
   {
      return null;
   }

   public List<?> find(String query, Object value, Type type)
   {
      return null;
   }

   public List<?> find(String query, Object[] values, Type[] types)
   {
      return null;
   }

   public Iterator<?> iterate(String query)
   {
      return null;
   }

   public Iterator<?> iterate(String query, Object value, Type type)
   {
      return null;
   }

   public Iterator<?> iterate(String query, Object[] values, Type[] types)
   {
      return null;
   }

   public Collection<?> filter(Object collection, String filter)
   {
      return null;
   }

   public Collection<?> filter(Object collection, String filter, Object value, Type type)
   {
      return null;
   }

   public Collection<?> filter(Object collection, String filter, Object[] values, Type[] types)
   {
      return null;
   }

   public int delete(String query)
   {
      return 0;
   }

   public int delete(String query, Object value, Type type)
   {
      return 0;
   }

   public int delete(String query, Object[] values, Type[] types)
   {
      return 0;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Query createSQLQuery(String sql, String returnAlias, Class returnClass)
   {
      return null;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Query createSQLQuery(String sql, String[] returnAliases, Class[] returnClasses)
   {
      return null;
   }

   public void save(Object object, Serializable id)
   {
   }

   public void save(String entityName, Object object, Serializable id)
   {
   }

   public void update(Object object, Serializable id)
   {
   }

   public void update(String entityName, Object object, Serializable id)
   {
   }
}