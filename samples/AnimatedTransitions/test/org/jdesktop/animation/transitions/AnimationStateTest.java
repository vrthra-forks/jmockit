/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.transitions;

import java.awt.*;
import javax.swing.*;

import org.junit.*;

import mockit.*;

import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.transitions.effects.*;
import static org.junit.Assert.*;

public final class AnimationStateTest
{
   final JComponent component = new JButton();

   @Test
   public void testCreateAnimationStartStateFromComponentState()
   {
      ComponentState componentState = new ComponentState(component);
      AnimationState state = new AnimationState(componentState, true);

      assertSame(component, state.getComponent());
      assertSame(componentState, state.getStart());
      assertNull(state.getEnd());
   }

   @Test
   public void testCreateAnimationEndStateFromComponentState()
   {
      ComponentState componentState = new ComponentState(component);
      AnimationState state = new AnimationState(componentState, false);

      assertSame(component, state.getComponent());
      assertNull(state.getStart());
      assertSame(componentState, state.getEnd());
   }

   @Test
   public void testCreateAnimationStartStateFromComponent()
   {
      AnimationState state = new AnimationState(component, true);

      assertSame(component, state.getComponent());
      assertEquals(new ComponentState(component), state.getStart());
      assertNull(state.getEnd());
   }

   @Test
   public void testCreateAnimationEndStateFromComponent()
   {
      AnimationState state = new AnimationState(component, false);

      assertSame(component, state.getComponent());
      assertNull(state.getStart());
      assertEquals(new ComponentState(component), state.getEnd());
   }

   @Test
   public void testResetStartState()
   {
      AnimationState state = new AnimationState(component, true);
      ComponentState initialStartState = state.getStart();

      state.setStart(new ComponentState(component));

      assertNotSame(initialStartState, state.getStart());
   }

   @Test
   public void testResetEndState()
   {
      AnimationState state = new AnimationState(component, false);
      ComponentState initialEndState = state.getEnd();

      state.setEnd(new ComponentState(component));

      assertNotSame(initialEndState, state.getEnd());
   }

   @Test
   public void initWithStartStateOnly(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, true);

      new Expectations()
      {
         final FadeOut effect = new FadeOut(state.getStart());

         {
            effect.init(animator, null);
         }
      };

      state.init(animator);
   }

   @Test
   public void initWithEndStateOnly(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, false);

      new Expectations()
      {
         final FadeIn effect = new FadeIn(state.getEnd());

         {
            effect.init(animator, null);
         }
      };

      state.init(animator);
   }

   @Test
   public void initWithBothStartAndEndStates(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, true);
      state.setEnd(new ComponentState(component));

      new Expectations()
      {
         final Unchanging effect = new Unchanging(state.getStart(), state.getEnd());

         {
            effect.init(animator, null);
         }
      };

      state.init(animator);
   }

   @Test
   public void initWithStartAndEndStatesInDifferentLocations(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, true);

      component.setLocation(20, 15);
      state.setEnd(new ComponentState(component));

      new Expectations()
      {
         final Move move = new Move(state.getStart(), state.getEnd());

         {
            move.init(animator, null);
         }
      };

      state.init(animator);
   }

   @Test
   public void initWithStartAndEndStatesHavingDifferentSizes(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, true);

      component.setSize(200, 150);
      state.setEnd(new ComponentState(component));

      new Expectations()
      {
         final Scale scale = new Scale(state.getStart(), state.getEnd());

         {
            scale.init(animator, null);
         }
      };

      state.init(animator);
   }

   @Test
   public void initWithStartAndEndStatesHavingDifferentLocationsAndSizes(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, true);

      component.setBounds(20, 15, 200, 150);
      state.setEnd(new ComponentState(component));

      new Expectations()
      {
         final Move move;
         final Scale scale;
         final CompositeEffect composite;

         {
            move = new Move(state.getStart(), state.getEnd());
            scale = new Scale(state.getStart(), state.getEnd());
            composite = new CompositeEffect(move);
            composite.addEffect(scale);
            composite.init(animator, null);
         }
      };

      state.init(animator);
   }

   @Test
   public void initWithStartStateOnlyForComponentWithCustomEffect(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, true);

      new Expectations()
      {
         Effect customEffect;

         {
            TransitionType.DISAPPEARING.setEffect(component, customEffect);

            customEffect.setStart(state.getStart());
            customEffect.init(animator, null);
         }
      };

      state.init(animator);
   }

   @Test
   public void initWithEndStateOnlyForComponentWithCustomEffect(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, false);

      new Expectations()
      {
         Effect customEffect;

         {
            TransitionType.APPEARING.setEffect(component, customEffect);

            customEffect.setEnd(state.getEnd());
            customEffect.init(animator, null);
         }
      };

      state.init(animator);
   }

   @Test
   public void initWithBothStartAndEndStatesForComponentWithCustomEffect(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, true);
      state.setEnd(new ComponentState(component));

      new Expectations()
      {
         Effect effect;
         Move customEffect;

         {
            TransitionType.CHANGING.setEffect(component, customEffect);

            customEffect.setStart(state.getStart());
            customEffect.setEnd(state.getEnd());
            customEffect.init(animator, null);
         }
      };

      state.init(animator);
   }

   @Test
   public void testCleanupState(final Animator animator)
   {
      final AnimationState state = new AnimationState(component, true);

      new Expectations()
      {
         FadeOut effect;

         {
            setField(state, effect);

            effect.cleanup(animator);
         }
      };

      state.cleanup(animator);
   }

   @Test
   public void testPaintState(final Graphics2D graphics2D)
   {
      final AnimationState state = new AnimationState(component, true);

      // Does nothing when no effect yet defined.
      state.paint(null);

      // Test painting with a defined effect:
      new Expectations()
      {
         Animator animator;
         Effect effect;

         {
            setField(state, effect);

            graphics2D.create(); returns(graphics2D);
            effect.render(graphics2D);
            graphics2D.dispose();
         }
      };

      state.paint(graphics2D);
   }
}
