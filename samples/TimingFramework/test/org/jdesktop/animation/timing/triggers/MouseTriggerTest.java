/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.jdesktop.animation.timing.triggers;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;

public final class MouseTriggerTest
{
   @Test
   public void addTrigger()
   {
      Component button = new JButton("Test");

      MouseTrigger trigger = MouseTrigger.addTrigger(button, null, MouseTriggerEvent.ENTER);

      MouseListener[] mouseListeners = button.getMouseListeners();
      MouseListener listenerAdded = mouseListeners[mouseListeners.length - 1];
      assertSame(trigger, listenerAdded);
   }

   @Test
   public void addTriggerWithAutoReverse()
   {
      Component button = new JButton("Test");

      MouseTrigger trigger = MouseTrigger.addTrigger(button, null, MouseTriggerEvent.ENTER, true);

      MouseListener[] mouseListeners = button.getMouseListeners();
      MouseListener listenerAdded = mouseListeners[mouseListeners.length - 1];
      assertSame(trigger, listenerAdded);
   }

   @Test
   public void mouseEntered()
   {
      MouseListener trigger = createMouseTriggerWithExpectationOnGivenEvent(MouseTriggerEvent.ENTER);
      trigger.mouseEntered(null);
   }

   private MouseListener createMouseTriggerWithExpectationOnGivenEvent(final MouseTriggerEvent event)
   {
      MouseTrigger mouseTrigger = new MouseTrigger(null, event);
      new Expectations() { @Mocked Trigger base; { base.fire(event); }};
      return mouseTrigger;
   }

   @Test
   public void mouseExited()
   {
      MouseListener trigger = createMouseTriggerWithExpectationOnGivenEvent(MouseTriggerEvent.EXIT);
      trigger.mouseExited(null);
   }

   @Test
   public void mousePressed()
   {
      MouseListener trigger = createMouseTriggerWithExpectationOnGivenEvent(MouseTriggerEvent.PRESS);
      trigger.mousePressed(null);
   }

   @Test
   public void mouseReleased()
   {
      MouseListener trigger = createMouseTriggerWithExpectationOnGivenEvent(MouseTriggerEvent.RELEASE);
      trigger.mouseReleased(null);
   }

   @Test
   public void mouseClicked()
   {
      MouseListener trigger = createMouseTriggerWithExpectationOnGivenEvent(MouseTriggerEvent.CLICK);
      trigger.mouseClicked(null);
   }
}
