package org.hibernate;

public interface ScrollableResults
{
   boolean next();
   Object get(int i);
}
