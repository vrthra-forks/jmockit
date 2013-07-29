package org.hibernate;

import java.util.*;

public interface Query
{
   List list();
   Query setParameter(int i, Object arg);
   Query setMaxResults(int i);
   Iterator<?> iterate();
}
