/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.transitions;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.*;

import org.junit.*;

import mockit.*;

import static mockit.Deencapsulation.*;
import org.jdesktop.animation.timing.*;
import org.jdesktop.animation.timing.interpolation.*;
import static org.junit.Assert.*;

@UsingMocksAndStubs({Animator.class, PropertySetter.class})
public final class ScreenTransitionContainerResizeTest
{
   @Mocked("createImage(int, int)") JComponent container;
   @NonStrict BufferedImage transitionImage;
   @NonStrict AnimationManager manager;

   Dimension newSize;
   ScreenTransition transition;

   @Before
   public void setUp()
   {
      container = new JPanel();
      newSize = new Dimension(100, 80);
   }

   @Test
   public void resizeTransitionContainerOnce()
   {
      new CreationOfTransitionImageExpectations();

      ComponentListener containerSizeListener = createTransition();

      // Exercise the code under test:
      assertResizingOfContainer(containerSizeListener);
   }

   final class CreationOfTransitionImageExpectations extends Expectations
   {
      {
         container.createImage(newSize.width, newSize.height); result = transitionImage;
         manager.recreateImage();
      }
   }

   private ComponentListener createTransition()
   {
      // Creates ScreenTransition, which adds itself as listener to the container:
      transition = new ScreenTransition(container, null, 100);

      // Get the internal listener:
      ComponentListener containerSizeListener = container.getListeners(ComponentListener.class)[0];

      // Remove the listener from the container, so it won't get called on "setSize":
      container.removeComponentListener(containerSizeListener);
      
      return containerSizeListener;
   }

   private void assertResizingOfContainer(ComponentListener containerSizeListener)
   {
      container.setSize(newSize);
      containerSizeListener.componentResized(null);
      assertSame(transitionImage, transition.getTransitionImage());
   }

   @Test
   public void changeWidthOfTransitionContainerAlreadyWithTransitionImage()
   {
      ComponentListener containerSizeListener = createTransition();
      setField(transition, transitionImage);

      new CreationOfTransitionImageExpectations();

      assertResizingOfContainer(containerSizeListener);
   }

   @Test
   public void changeHeightOfTransitionContainerAlreadyWithTransitionImage()
   {
      ComponentListener containerSizeListener = createTransition();
      setField(transition, transitionImage);

      new Expectations() {{ transitionImage.getWidth(); result = newSize.width; }};
      new CreationOfTransitionImageExpectations();

      assertResizingOfContainer(containerSizeListener);
   }
}
