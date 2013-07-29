/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.net.*;
import javax.naming.*;

import static org.junit.Assert.*;
import org.junit.*;

public final class CustomClassLoadingTest
{
   @Test
   public void changeContextClassLoaderDuringReplay(final InitialContext ic) throws Exception
   {
      // Uses TestRun instance associated with current context CL:
      new Expectations() {{ ic.lookup(anyString); result = "mocked"; }};

      // OpenEJB does this whenever a method is called on an EJB:
      Thread t = Thread.currentThread();
      ClassLoader cl = t.getContextClassLoader();
      URLClassLoader childOfSystemCL = new URLClassLoader(new URL[0]);
      t.setContextClassLoader(childOfSystemCL);

      // Replay with a different context CL; must use same TestRun instance:
      assertEquals("mocked", ic.lookup("test"));

      t.setContextClassLoader(cl);
   }
}
