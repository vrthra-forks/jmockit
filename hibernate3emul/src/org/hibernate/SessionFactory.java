package org.hibernate;

import org.hibernate.classic.Session;

public interface SessionFactory
{
   Session openSession();
}
