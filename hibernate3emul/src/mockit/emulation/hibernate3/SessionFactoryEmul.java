/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3;

import java.io.*;
import java.sql.*;
import java.util.*;
import javax.naming.*;

import org.hibernate.*;
import org.hibernate.classic.Session;
import org.hibernate.engine.*;
import org.hibernate.metadata.*;
import org.hibernate.stat.*;

final class SessionFactoryEmul implements SessionFactory
{
   public Session openSession(Connection connection)
   {
      return openSession();
   }

   public Session openSession(Interceptor interceptor)
   {
      return openSession();
   }

   public Session openSession(Connection connection, Interceptor interceptor)
   {
      return null;
   }

   public Session openSession()
   {
      return new ClassicSessionEmul(this);
   }

   public Session getCurrentSession()
   {
      return null;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public ClassMetadata getClassMetadata(Class persistentClass)
   {
      return null;
   }

   public ClassMetadata getClassMetadata(String entityName)
   {
      return null;
   }

   public CollectionMetadata getCollectionMetadata(String roleName)
   {
      return null;
   }

   public Map<?, ?> getAllClassMetadata()
   {
      return null;
   }

   public Map<?, ?> getAllCollectionMetadata()
   {
      return null;
   }

   public Statistics getStatistics()
   {
      return null;
   }

   public void close()
   {
   }

   public boolean isClosed()
   {
      return false;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public void evict(Class persistentClass)
   {
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public void evict(Class persistentClass, Serializable id)
   {
   }

   public void evictEntity(String entityName)
   {
   }

   public void evictEntity(String entityName, Serializable id)
   {
   }

   public void evictCollection(String roleName)
   {
   }

   public void evictCollection(String roleName, Serializable id)
   {
   }

   public void evictQueries()
   {
   }

   public void evictQueries(String cacheRegion)
   {
   }

   public StatelessSession openStatelessSession()
   {
      return null;
   }

   public StatelessSession openStatelessSession(Connection connection)
   {
      return null;
   }

   public Set<?> getDefinedFilterNames()
   {
      return null;
   }

   public FilterDefinition getFilterDefinition(String filterName)
   {
      return null;
   }

   public Reference getReference()
   {
      return null;
   }
}
