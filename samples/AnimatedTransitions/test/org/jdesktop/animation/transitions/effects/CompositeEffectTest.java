/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.transitions.effects;

import java.awt.*;
import java.awt.image.*;
import java.util.List;

import org.junit.*;

import mockit.*;

import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.transitions.*;
import static org.junit.Assert.*;

public final class CompositeEffectTest
{
   @Mocked ComponentState start;
   @Mocked ComponentState end;

   CompositeEffect composite;
   Effect effect1;
   Effect effect2;

   @Before
   public void setUp()
   {
      composite = new CompositeEffect();
      effect1 = new Unchanging();
      effect2 = new Unchanging();
   }

   @Test
   public void testAddEffect()
   {
      Effect effect = new Move(start, end);
      effect.setRenderComponent(true);

      composite.addEffect(effect);

      assertEffectWasAdded(effect);
      assertTrue(composite.getRenderComponent());
      assertSame(start, composite.getStart());
      assertSame(end, composite.getEnd());
   }

   private void assertEffectWasAdded(Effect effect)
   {
      @SuppressWarnings("unchecked") List<Effect> effects = Deencapsulation.getField(composite, List.class);
      assertTrue(effects.contains(effect));
   }

   @Test
   public void testAddEffectWhichDoesNotRequireRerendering()
   {
      Effect effect = new Move(start, end);

      composite.addEffect(effect);

      assertEffectWasAdded(effect);
      assertFalse(composite.getRenderComponent());
   }

   @Test
   public void testAddEffectWhenStartAndEndStatesAreAlreadySet(ComponentState anotherStart, ComponentState anotherEnd)
   {
      Effect effect = new Move(start, end);
      composite.setStart(anotherStart);
      composite.setEnd(anotherEnd);

      composite.addEffect(effect);

      assertEffectWasAdded(effect);
      assertSame(anotherStart, composite.getStart());
      assertSame(anotherEnd, composite.getEnd());
   }

   @Test
   public void testSetStart()
   {
      composite.addEffect(effect1);
      composite.addEffect(effect2);

      new Expectations(Effect.class)
      {
         private final Effect effectSuper = new Unchanging();

         {
            effect1.setStart(start);
            effect2.setStart(start);
            onInstance(composite); effectSuper.setStart(start);
         }
      };

      composite.setStart(start);
   }

   @Test
   public void testSetEnd()
   {
      composite.addEffect(effect1);
      composite.addEffect(effect2);

      new Expectations(Effect.class)
      {
         private final Effect effectSuper = new Unchanging();

         {
            effect1.setEnd(end);
            effect2.setEnd(end);
            onInstance(composite); effectSuper.setEnd(end);
         }
      };

      composite.setEnd(end);
   }

   @Test
   public void testInit(final Animator animator)
   {
      composite.addEffect(effect1);
      composite.addEffect(effect2);

      new Expectations(Effect.class)
      {
         private final Effect effectSuper = new Unchanging();

         {
            effect1.init(animator, composite);
            effect2.init(animator, composite);
            effectSuper.init(animator, null);
         }
      };

      composite.init(animator, null);
   }

   @Test
   public void testCleanup(final Animator animator)
   {
      composite.addEffect(effect1);

      new Expectations(Effect.class)
      {
         {
            onInstance(effect1).cleanup(animator);
         }
      };

      composite.cleanup(animator);
   }

   @Test
   public void testSetup()
   {
      composite = new CompositeEffect(effect1);
      composite.addEffect(effect2);

      new CompositeSetupExpectations();

      composite.setup(null);
   }

   final class CompositeSetupExpectations extends Expectations
   {
      CompositeSetupExpectations()
      {
         super(Effect.class);

         onInstance(effect1).setup((Graphics2D) any);
         onInstance(effect2).setup((Graphics2D) any);

         Effect effectSuper = new Unchanging();
         onInstance(composite); effectSuper.setup((Graphics2D) any);
      }
   }

   @Test
   public void testSetupWhenComponentNeedsRerendering()
   {
      composite = new CompositeEffect(effect1);
      composite.addEffect(effect2);
      composite.setRenderComponent(true);

      new CompositeSetupExpectations();

      composite.setup(null);

      assertNull(composite.getComponentImage());
   }

   @Test
   public void testSetupWhenComponentImageHasBeenSetupAlready()
   {
      composite = new CompositeEffect(effect1);
      composite.addEffect(effect2);
      Image compositeImage = new BufferedImage(10, 5, BufferedImage.TYPE_BYTE_GRAY);
      Deencapsulation.setField(composite, compositeImage);

      new CompositeSetupExpectations();

      composite.setup(null);

      assertSame(compositeImage, composite.getComponentImage());
   }

   @Test
   public void testOperationsWithNoSubEffects(final Animator animator)
   {
      new Expectations(Effect.class)
      {
         private final Effect effectSuper = new Unchanging();

         {
            effectSuper.init(animator, null);
            effectSuper.setStart(null);
            effectSuper.setup(null);
            effectSuper.setEnd(null);
         }
      };

      composite.init(animator, null);
      composite.setStart(null);
      composite.setup(null);
      composite.setEnd(null);
      composite.cleanup(animator);
   }
}
