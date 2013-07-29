/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.transitions;

import java.awt.*;
import java.awt.image.*;
import javax.swing.*;

import org.junit.*;

import mockit.*;

import static junit.framework.Assert.*;
import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;

@UsingMocksAndStubs(PropertySetter.class)
public final class ScreenTransitionExecutionTest
{
   @Mocked JComponent container;
   @Mocked TransitionTarget target;
   @Mocked Animator animator;
   @Mocked AnimationManager manager;
   @Mocked AnimationLayer animationLayer;
   @Mocked("()") @Capturing(maxInstances = 1) TimingTarget timingTarget;
   ScreenTransition transition;

   @Before
   public void createTransition()
   {
      transition = new ScreenTransition(container, target, animator);
   }

   @Test
   public void endTransition()
   {
      new NonStrictExpectations() {
         JRootPane rootPane;
         Component savedGlassPane;

         {
            container.getRootPane(); returns(rootPane);
            setField(transition, savedGlassPane);
         }
      };

      timingTarget.end();

      new Verifications() {{
         animationLayer.setVisible(false);
         container.setVisible(true);
         manager.reset(animator);
      }};
   }

   @Test
   public void beginTransition(final Graphics2D g2D)
   {
      new NonStrictExpectations() {
         final int width = 200;
         final int height = 150;
         JRootPane rootPane;
         BufferedImage transitionImage;

         {
            container.getWidth(); returns(width);
            container.getHeight(); returns(height);

            container.createImage(width, height); returns(transitionImage);

            container.getRootPane(); returns(rootPane);
            transitionImage.getGraphics(); returns(g2D);
         }
      };

      timingTarget.begin();
      assertNotNull(transition.getTransitionImage());

      new VerificationsInOrder() {{
         manager.setupStart();
         animationLayer.setupBackground(container);
         target.setupNextScreen();
         manager.setupEnd();
         manager.init(animator);
         manager.paint(g2D);
      }};
   }
}
