/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3;

import mockit.*;
import org.hibernate.*;
import org.hibernate.cfg.*;

public final class ConfigurationEmul extends MockUp<Configuration>
{
   @Mock
   public static void $clinit() {}

   @Mock
   public static void reset() {}

   @Mock
   public static Configuration configure(Invocation invocation)
   {
      return invocation.getInvokedInstance();
   }

   @Mock
   public static SessionFactory buildSessionFactory()
   {
      return new SessionFactoryEmul();
   }
}
