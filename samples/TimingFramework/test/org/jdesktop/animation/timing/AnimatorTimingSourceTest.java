/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.timing;

import static org.jdesktop.animation.timing.Animator.*;
import static org.junit.Assert.*;
import org.junit.*;

import mockit.*;

public final class AnimatorTimingSourceTest
{
   @Test
   public void setTimer(final TimingSource timingSource)
   {
      final Animator animator = new Animator(500);

      new Expectations() {{
         // Expectations for setTimer:
         timingSource.addEventListener(withInstanceOf(TimingEventListener.class));
         timingSource.setResolution(animator.getResolution());
         timingSource.setStartDelay(animator.getStartDelay());

         // Expectations for notification of events:
         timingSource.start();
         timingSource.stop();
      }};

      animator.setTimer(timingSource);
      animator.start();
      animator.stop();
   }

   @Test
   public void setTimerToCustomTimingSourceThenResetBackToOriginal(final TimingSource timingSource)
   {
      Animator animator = new Animator(50);

      new Expectations() {{
         timingSource.addEventListener(withInstanceOf(TimingEventListener.class));
         timingSource.setResolution(anyInt);
         timingSource.setStartDelay(anyInt);
         timingSource.removeEventListener(withInstanceOf(TimingEventListener.class));
      }};

      animator.setTimer(timingSource);
      animator.setTimer(null);
   }

   private static final class EmptyTimingSource extends TimingSource
   {
      @Override
      public void start() {}

      @Override
      public void stop() {}

      @Override
      public void setResolution(int resolution) {}

      @Override
      public void setStartDelay(int delay) {}
   }

   @Test
   public void timingEventOnTimingSource()
   {
      Animator animator = new Animator(50);
      final TimingSource timingSource = new EmptyTimingSource();

      new Expectations() {
         @Capturing
         private TimingEventListener timingEventTarget;

         {
            timingEventTarget.timingSourceEvent(timingSource);
         }
      };

      animator.setTimer(timingSource);
      timingSource.timingEvent();
   }

   @Test
   public void timingSourceEventOnTimingSourceTargetForNonRunningAnimator()
   {
      final Animator animator = new Animator(50);
      TimingSource timingSource = new EmptyTimingSource();
      animator.setTimer(timingSource);

      new Expectations() {
         TimingTarget timingTarget;

         {
            // Passing mock object into code under test:
            animator.addTarget(timingTarget);

            // Expectations:
            timingTarget.timingEvent(0.0f);
            times = 0; // Animator is not running, so no timing event is expected.
         }
      };

      timingSource.timingEvent();
   }

   @Test
   public void timingSourceEventOnTimingSourceTargetForRunningAnimator()
   {
      final Animator animator = new Animator(50);
      TimingSource timingSource = new EmptyTimingSource();
      animator.setTimer(timingSource);

      new Expectations() {
         TimingTarget timingTarget;

         {
            // Passing mock object into code under test:
            animator.addTarget(timingTarget);

            // Expectations:
            timingTarget.begin();
            timingTarget.timingEvent(withEqual(0.0f, 0.04));
         }
      };

      animator.start();
      timingSource.timingEvent();
   }

   @Test
   public void timingSourceEventOnTimingSourceTargetForRunningAnimatorAtTimeToStop()
   {
      final Animator animator = new Animator(50);
      TimingSource timingSource = new EmptyTimingSource();
      animator.setTimer(timingSource);

      new Expectations() {
         @Mocked("nanoTime") final System system = null;
         @Mocked TimingTarget timingTarget;

         {
            animator.addTarget(timingTarget);

            // For the call to animator.start():
            System.nanoTime(); result = 0L;

            // For the call to timingSource.timingEvent():
            System.nanoTime(); result = 50L * 1000000;

            // Resulting expected interactions:
            timingTarget.begin();
            timingTarget.timingEvent(1.0f);
            timingTarget.end();
         }
      };

      animator.start();
      timingSource.timingEvent();
   }

   @Test
   public void timingSourceEventOnTimingSourceTargetForRunningRepeatingAnimator()
   {
      final Animator animator = new Animator(50, INFINITE, RepeatBehavior.LOOP, null);
      TimingSource timingSource = new EmptyTimingSource();
      animator.setTimer(timingSource);

      new Expectations() {
         @Mocked("nanoTime") final System system = null;
         @Mocked TimingTarget timingTarget;

         {
            animator.addTarget(timingTarget);

            System.nanoTime(); returns(0L, 60L * 1000000);

            timingTarget.begin();
            timingTarget.repeat();
            timingTarget.timingEvent(anyFloat);
            forEachInvocation = new Object() {
               void validate(float item) { assertTrue(item > 0.0f); }
            };
         }
      };

      animator.start();
      timingSource.timingEvent();
   }
}
