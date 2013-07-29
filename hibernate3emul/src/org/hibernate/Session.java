package org.hibernate;

import java.io.*;
import java.sql.*;

public interface Session
{
   Object load(Class<?> entityClass, Serializable entityId);
   Object get(Class<?> entityClass, Serializable entityId);
   void evict(Object entity);
   Serializable save(Object entityData);
   void delete(Object entity);
   Query createQuery(String ql);
   void flush();
   Transaction beginTransaction();
   void clear();
   Connection close();
   Transaction getTransaction();
   Serializable getIdentifier(Object entity);
}
