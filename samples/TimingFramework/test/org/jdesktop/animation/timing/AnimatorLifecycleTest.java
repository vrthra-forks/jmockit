/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.timing;

import org.junit.*;

import mockit.*;

import static org.jdesktop.animation.timing.Animator.*;
import static org.junit.Assert.*;

public final class AnimatorLifecycleTest
{
   @Capturing TimingSource timer;
   Animator animator;

   @Before
   public void setUp()
   {
      TimingSource mockTimer = timer;
      animator = new Animator(500);
      assertNotSame(mockTimer, timer);
   }

   @Test
   public void testStart()
   {
      assertFalse(animator.isRunning());

      new Expectations()
      {
         {
            timer.start();
         }
      };

      animator.start();

      assertTrue(animator.isRunning());
   }

   @Test
   public void testStartForwardAtIntermediateFraction()
   {
      animator.setStartFraction(0.2f);

      new Expectations()
      {
         {
            timer.start();
         }
      };

      animator.start();

      assertTrue(animator.isRunning());
   }

   @Test
   public void testStartBackwardAtIntermediateFraction()
   {
      animator.setStartDirection(Direction.BACKWARD);
      animator.setStartFraction(0.8f);

      new Expectations()
      {
         {
            timer.start();
         }
      };

      animator.start();

      assertTrue(animator.isRunning());
   }

   @Test
   public void testStop()
   {
      new Expectations()
      {
         {
            timer.start();
            timer.stop();
         }
      };

      animator.start();
      assertTrue(animator.isRunning());
      animator.stop();
      assertFalse(animator.isRunning());
   }

   @Test
   public void testCancel()
   {
      new Expectations()
      {
         {
            timer.start();
            timer.stop();
         }
      };

      animator.start();
      animator.cancel();
      assertFalse(animator.isRunning());
   }

   @Test
   public void testPause()
   {
      new Expectations()
      {
         {
            timer.start();
            timer.stop();
         }
      };

      animator.start();
      animator.pause();
      assertFalse(animator.isRunning());
   }

   @Test
   public void testResume()
   {
      new Expectations()
      {
         {
            timer.start();
            timer.stop();
            timer.start();
         }
      };

      animator.start();
      animator.pause();
      assertFalse(animator.isRunning());
      animator.resume();
      assertTrue(animator.isRunning());
   }

   @Test(expected = IllegalStateException.class)
   public void testChangeConfigurationWhileRunning()
   {
      new Expectations()
      {
         {
            timer.start();
         }
      };

      animator.start();
      animator.setDuration(100);
   }
}
