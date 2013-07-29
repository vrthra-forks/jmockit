package org.hibernate.cfg;

import org.hibernate.*;

public class Configuration
{
   public Configuration configure() { return this; }
   public SessionFactory buildSessionFactory() { return null; }
   public void reset() {}
}
