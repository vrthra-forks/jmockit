/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package integrationTests;

import org.junit.*;

public final class SeventhTest
{
   @BeforeClass
   public static void initializeCounter()
   {
      Dependency.counter = 0;
   }

   @Before
   public void setUp()
   {
      assert Dependency.counter == 0 || Dependency.counter == 1;
   }

   @After
   public void tearDown()
   {
      assert Dependency.counter == 1;
   }

   @Test
   public void slowTest1() throws Exception
   {
      TestedClass.doSomething(true);
      Thread.sleep(280);
   }

   @Test
   public void slowTest2() throws Exception
   {
      TestedClass.doSomething(false);
      Thread.sleep(720);
   }
}
