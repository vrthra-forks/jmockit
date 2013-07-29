/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3;

import java.io.*;
import java.math.*;
import java.util.*;

import mockit.emulation.hibernate3.ast.*;
import org.hibernate.*;
import org.hibernate.transform.*;
import org.hibernate.type.*;

@SuppressWarnings({"ClassWithTooManyMethods", "OverlyComplexClass"})
final class QueryEmul implements Query
{
   private final Collection<?> entities;
   private final String hql;
   private final QueryAST ast;

   private int maxResults;
   private int firstResult;

   QueryEmul(String hql, Collection<?> persistentEntities) throws QuerySyntaxException
   {
      entities = persistentEntities;
      this.hql = hql;
      ast = new QueryAST(hql);
   }

   public String getQueryString()
   {
      return hql;
   }

   public Type[] getReturnTypes()
   {
      return new Type[0];
   }

   public String[] getReturnAliases()
   {
      return new String[0];
   }

   public String[] getNamedParameters()
   {
      return new String[0];
   }

   public Iterator<?> iterate()
   {
      return list().iterator();
   }

   public ScrollableResults scroll()
   {
      return new ScrollableResultsEmul(list());
   }

   public ScrollableResults scroll(ScrollMode scrollMode)
   {
      return new ScrollableResultsEmul(list());
   }

   public List<?> list()
   {
      List<?> result = ast.matches(entities);

      if (firstResult > 0 && maxResults > 0) {
         result = pagedResult(result, firstResult, firstResult + maxResults);
      }
      else if (firstResult > 0 && maxResults <= 0) {
         result = pagedResult(result, firstResult, result.size());
      }
      else if (firstResult <= 0 && maxResults > 0) {
         result = pagedResult(result, 0, maxResults);
      }

      return result;
   }

   private List<?> pagedResult(List<?> result, int fromIndex, int toIndex)
   {
      int resultSize = result.size();

      if (fromIndex >= resultSize) {
         return new ArrayList<Object>(0);
      }

      return result.subList(fromIndex, toIndex <= resultSize ? toIndex : resultSize);
   }

   public Object uniqueResult()
   {
      List<?> result = list();
      int resultCount = result.size();

      if (resultCount == 1) {
         return result.get(0);
      }
      else if (resultCount == 0) {
         return null;
      }
      else {
         throw new NonUniqueResultException(resultCount);
      }
   }

   public int executeUpdate()
   {
      return 0;
   }

   public Query setMaxResults(int maxResults)
   {
      this.maxResults = maxResults;
      return this;
   }

   public Query setFirstResult(int firstResult)
   {
      this.firstResult = firstResult;
      return this;
   }

   public Query setReadOnly(boolean readOnly)
   {
      return null;
   }

   public Query setCacheable(boolean cacheable)
   {
      return null;
   }

   public Query setCacheRegion(String cacheRegion)
   {
      return null;
   }

   public Query setTimeout(int timeout)
   {
      return null;
   }

   public Query setFetchSize(int fetchSize)
   {
      return null;
   }

   public Query setLockMode(String alias, LockMode lockMode)
   {
      return null;
   }

   public Query setComment(String comment)
   {
      return null;
   }

   public Query setFlushMode(FlushMode flushMode)
   {
      return null;
   }

   public Query setCacheMode(CacheMode cacheMode)
   {
      return null;
   }

   public Query setParameter(int position, Object val, Type type)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setParameter(String name, Object val, Type type)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setParameter(int position, Object val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setParameter(String name, Object val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setParameters(Object[] values, Type[] types)
   {
      return null;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Query setParameterList(String name, Collection vals, Type type)
   {
      ast.setParameter(name, vals);
      return this;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Query setParameterList(String name, Collection vals)
   {
      ast.setParameter(name, vals);
      return this;
   }

   public Query setParameterList(String name, Object[] vals, Type type)
   {
      ast.setParameter(name, vals);
      return this;
   }

   public Query setParameterList(String name, Object[] vals)
   {
      ast.setParameter(name, vals);
      return this;
   }

   public Query setProperties(Object bean)
   {
      return null;
   }

   @SuppressWarnings({"RawUseOfParameterizedType"})
   public Query setProperties(Map bean)
   {
      return null;
   }

   public Query setString(int position, String val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setCharacter(int position, char val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setBoolean(int position, boolean val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setByte(int position, byte val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setShort(int position, short val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setInteger(int position, int val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setLong(int position, long val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setFloat(int position, float val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setDouble(int position, double val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setBinary(int position, byte[] val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setText(int position, String val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setSerializable(int position, Serializable val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setLocale(int position, Locale locale)
   {
      ast.setParameter(position, locale);
      return this;
   }

   public Query setBigDecimal(int position, BigDecimal number)
   {
      ast.setParameter(position, number);
      return this;
   }

   public Query setBigInteger(int position, BigInteger number)
   {
      ast.setParameter(position, number);
      return this;
   }

   public Query setDate(int position, Date date)
   {
      ast.setParameter(position, date);
      return this;
   }

   public Query setTime(int position, Date date)
   {
      ast.setParameter(position, date);
      return this;
   }

   public Query setTimestamp(int position, Date date)
   {
      ast.setParameter(position, date);
      return this;
   }

   public Query setCalendar(int position, Calendar calendar)
   {
      ast.setParameter(position, calendar);
      return this;
   }

   public Query setCalendarDate(int position, Calendar calendar)
   {
      ast.setParameter(position, calendar);
      return this;
   }

   public Query setString(String name, String val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setCharacter(String name, char val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setBoolean(String name, boolean val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setByte(String name, byte val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setShort(String name, short val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setInteger(String name, int val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setLong(String name, long val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setFloat(String name, float val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setDouble(String name, double val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setBinary(String name, byte[] val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setText(String name, String val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setSerializable(String name, Serializable val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setLocale(String name, Locale locale)
   {
      ast.setParameter(name, locale);
      return this;
   }

   public Query setBigDecimal(String name, BigDecimal number)
   {
      ast.setParameter(name, number);
      return this;
   }

   public Query setBigInteger(String name, BigInteger number)
   {
      ast.setParameter(name, number);
      return this;
   }

   public Query setDate(String name, Date date)
   {
      ast.setParameter(name, date);
      return this;
   }

   public Query setTime(String name, Date date)
   {
      ast.setParameter(name, date);
      return this;
   }

   public Query setTimestamp(String name, Date date)
   {
      ast.setParameter(name, date);
      return this;
   }

   public Query setCalendar(String name, Calendar calendar)
   {
      ast.setParameter(name, calendar);
      return this;
   }

   public Query setCalendarDate(String name, Calendar calendar)
   {
      ast.setParameter(name, calendar);
      return this;
   }

   public Query setEntity(int position, Object val)
   {
      ast.setParameter(position, val);
      return this;
   }

   public Query setEntity(String name, Object val)
   {
      ast.setParameter(name, val);
      return this;
   }

   public Query setResultTransformer(ResultTransformer transformer)
   {
      return null;
   }
}