/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.timing.triggers;

import org.jdesktop.animation.timing.*;

import org.junit.*;
import static org.junit.Assert.*;

import mockit.*;

public final class TimingTriggerTest
{
   @Test
   public void addTrigger(final Animator source, final Animator target)
   {
      final TimingTriggerEvent event = TimingTriggerEvent.START;

      new Expectations(TimingTrigger.class) {{
         source.addTarget(new TimingTrigger(target, event, false));
      }};

      TimingTrigger triggerAdded = TimingTrigger.addTrigger(source, target, event);

      assertNotNull(triggerAdded);
   }

   @Test
   public void addTriggerWithAutoReverse(final Animator source, final Animator target)
   {
      final TimingTriggerEvent event = TimingTriggerEvent.STOP;

      new Expectations(TimingTrigger.class) {{
         source.addTarget(new TimingTrigger(target, event, true));
      }};

      TimingTrigger triggerAdded = TimingTrigger.addTrigger(source, target, event, true);

      assertNotNull(triggerAdded);
   }

   @Test
   public void begin(@Mocked final Trigger base)
   {
      TimingTrigger timingTrigger = new TimingTrigger(null, TimingTriggerEvent.START);

      timingTrigger.begin();

      new Verifications() {{ base.fire(TimingTriggerEvent.START); }};
   }

   @Test
   public void end(@Mocked final Trigger base)
   {
      TimingTrigger timingTrigger = new TimingTrigger(null, TimingTriggerEvent.STOP);

      timingTrigger.end();

      new Verifications() {{ base.fire(TimingTriggerEvent.STOP); }};
   }

   @Test
   public void repeat(@Mocked final Trigger base)
   {
      TimingTrigger timingTrigger = new TimingTrigger(null, TimingTriggerEvent.REPEAT);

      timingTrigger.repeat();

      new Verifications() {{ base.fire(TimingTriggerEvent.REPEAT); }};
   }

   @Test
   public void timingEvent(@Mocked Trigger base)
   {
      TimingTrigger timingTrigger = new TimingTrigger(null, TimingTriggerEvent.STOP);

      timingTrigger.timingEvent(0.0f);

      // Makes sure no methods were called on the base class.
      new FullVerifications() {};
   }
}
