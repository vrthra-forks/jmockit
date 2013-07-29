/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3;

import java.io.*;
import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

import org.hibernate.*;
import org.hibernate.jdbc.*;
import org.hibernate.stat.*;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyCoupledClass"})
class SessionEmul implements Session
{
   final Map<Serializable, Object> entityCache = new HashMap<Serializable, Object>();
   final Map<Serializable, Object> loadedEntities = new HashMap<Serializable, Object>();
   final SessionFactory sessionFactory;
   Connection connection;
   FlushMode flushMode = FlushMode.AUTO;
   CacheMode cacheMode = CacheMode.NORMAL;
   boolean open;

   SessionEmul(SessionFactory sessionFactory, Connection connection)
   {
      this.sessionFactory = sessionFactory;
      this.connection = connection;
   }

   public EntityMode getEntityMode()
   {
      return null;
   }

   public Session getSession(EntityMode entityMode)
   {
      return null;
   }

   public void flush()
   {
   }

   public void setFlushMode(FlushMode flushMode)
   {
      this.flushMode = flushMode;
   }

   public FlushMode getFlushMode()
   {
      return flushMode;
   }

   public void setCacheMode(CacheMode cacheMode)
   {
      this.cacheMode = cacheMode;
   }

   public CacheMode getCacheMode()
   {
      return cacheMode;
   }

   public SessionFactory getSessionFactory()
   {
      return sessionFactory;
   }

   public Connection connection()
   {
      return connection;
   }

   public Connection close()
   {
      open = false;
      Connection currentConnection = connection;
      connection = null;
      return currentConnection;
   }

   public void cancelQuery()
   {
   }

   public boolean isOpen()
   {
      return open;
   }

   public boolean isConnected()
   {
      return connection != null;
   }

   public boolean isDirty()
   {
      return false;
   }

   public Serializable getIdentifier(Object object)
   {
      try {
         Method idGetter = object.getClass().getMethod("getId");
         return (Serializable) idGetter.invoke(object);
      }
      catch (NoSuchMethodException e) {
         throw new RuntimeException(e);
      }
      catch (IllegalAccessException e) {
         throw new RuntimeException(e);
      }
      catch (InvocationTargetException e) {
         throw new RuntimeException(e);
      }
   }

   public boolean contains(Object object)
   {
      return loadedEntities.containsValue(object);
   }

   public void evict(Object object)
   {
      Serializable id = getIdentifier(object);
      loadedEntities.remove(id);
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Object load(Class theClass, Serializable id, LockMode lockMode)
   {
      return load(theClass, id);
   }

   public Object load(String entityName, Serializable id, LockMode lockMode)
   {
      return null;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Object load(Class theClass, Serializable id) throws ObjectNotFoundException
   {
      Object entity = get(theClass, id);

      if (entity == null) {
         throw new ObjectNotFoundException(id, theClass.getName());
      }

      return entity;
   }

   public Object load(String entityName, Serializable id)
   {
      return null;
   }

   public void load(Object object, Serializable id)
   {
   }

   public void replicate(Object object, ReplicationMode replicationMode)
   {
   }

   public void replicate(String entityName, Object object, ReplicationMode replicationMode)
   {
   }

   public Serializable save(Object object)
   {
      Serializable id = getIdentifier(object);
      loadedEntities.put(id, object);
      entityCache.put(id, object);
      return id;
   }

   public Serializable save(String entityName, Object object)
   {
      return null;
   }

   public void saveOrUpdate(Object object)
   {
   }

   public void saveOrUpdate(String entityName, Object object)
   {
   }

   public void update(Object object)
   {
   }

   public void update(String entityName, Object object)
   {
   }

   public Object merge(Object object)
   {
      return null;
   }

   public Object merge(String entityName, Object object)
   {
      return null;
   }

   public void persist(Object object)
   {
      save(object);
   }

   public void persist(String entityName, Object object)
   {
   }

   public void delete(Object object)
   {
      Serializable id = getIdentifier(object);
      loadedEntities.remove(id);
      entityCache.remove(id);
   }

   public void delete(String entityName, Object object)
   {
   }

   public void lock(Object object, LockMode lockMode)
   {
   }

   public void lock(String entityName, Object object, LockMode lockMode)
   {
   }

   public void refresh(Object object)
   {
   }

   public void refresh(Object object, LockMode lockMode)
   {
   }

   public LockMode getCurrentLockMode(Object object)
   {
      return null;
   }

   public Transaction beginTransaction()
   {
      return null;
   }

   public Transaction getTransaction()
   {
      return new TransactionEmul();
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Criteria createCriteria(Class persistentClass)
   {
      return null;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Criteria createCriteria(Class persistentClass, String alias)
   {
      return null;
   }

   public Criteria createCriteria(String entityName)
   {
      return null;
   }

   public Criteria createCriteria(String entityName, String alias)
   {
      return null;
   }

   public Query createQuery(String queryString)
   {
      return new QueryEmul(queryString, entityCache.values());
   }

   public SQLQuery createSQLQuery(String queryString)
   {
      return null;
   }

   public Query createFilter(Object collection, String queryString)
   {
      return null;
   }

   public Query getNamedQuery(String queryName)
   {
      return null;
   }

   public void clear()
   {
      loadedEntities.clear();
      entityCache.clear();
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Object get(Class theClass, Serializable id)
   {
      Object entity = loadedEntities.get(id);

      if (entity == null) {
         entity = entityCache.get(id);

         if (entity != null) {
            loadedEntities.put(id, entity);
         }
      }

      return entity;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Object get(Class clazz, Serializable id, LockMode lockMode)
   {
      return null;
   }

   public Object get(String entityName, Serializable id)
   {
      return null;
   }

   public Object get(String entityName, Serializable id, LockMode lockMode)
   {
      return null;
   }

   public String getEntityName(Object object)
   {
      return null;
   }

   public Filter enableFilter(String filterName)
   {
      return null;
   }

   public Filter getEnabledFilter(String filterName)
   {
      return null;
   }

   public void disableFilter(String filterName)
   {
   }

   public SessionStatistics getStatistics()
   {
      return null;
   }

   public void setReadOnly(Object entity, boolean readOnly)
   {
   }

   public void doWork(Work work) throws HibernateException
   {
   }

   public Connection disconnect()
   {
      return null;
   }

   public void reconnect()
   {
   }

   public void reconnect(Connection connection)
   {
   }
}
