/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.transitions;

import java.awt.event.*;
import javax.swing.*;

import org.junit.*;

import mockit.*;

import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;
import static org.junit.Assert.*;

@UsingMocksAndStubs(PropertySetter.class)
public final class ScreenTransitionInitializationTest
{
   @NonStrict JComponent container;
   @Mocked TransitionTarget target;
   @Mocked Animator animator;
   @Mocked final AnimationManager manager = null;
   @Mocked AnimationLayer layer;

   @Test
   public void createScreenTransitionWithNonRunningAnimator()
   {
      new TransitionInitialization();
      new SettingOfAnimatorField();

      ScreenTransition st = new ScreenTransition(container, target, animator);

      assertSame(animator, st.getAnimator());
   }

   final class TransitionInitialization extends Expectations
   {
      {
         new AnimationManager(container);
         new AnimationLayer(withInstanceOf(ScreenTransition.class));
         layer.setVisible(false);
         onInstance(container).addComponentListener(withInstanceOf(ComponentListener.class));
         onInstance(container).getWidth(); result = 0;
         onInstance(container).getHeight(); result = 0;
      }
   }

   final class SettingOfAnimatorField extends Expectations
   {
      {
         onInstance(animator).isRunning(); result = false;
         onInstance(animator).addTarget(withInstanceOf(TimingTargetAdapter.class));
      }
   }

   @Test
   public void createScreenTransitionForGivenDuration()
   {
      new TransitionInitialization();

      // Create the animator to be used:
      final int duration = 100;

      new Expectations()
      {
         {
            animator = new Animator(duration, withInstanceOf(TimingTargetAdapter.class));
         }
      };

      new SettingOfAnimatorField();

      ScreenTransition st = new ScreenTransition(container, target, duration);
      
      assertNotNull(st.getAnimator());
   }
}
