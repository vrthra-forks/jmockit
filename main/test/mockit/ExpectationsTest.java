/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.net.*;
import java.util.*;

import org.junit.*;

import static org.junit.Assert.*;

import mockit.internal.*;

public final class ExpectationsTest
{
   static class Collaborator
   {
      private int value;

      Collaborator() {}
      Collaborator(int value) { this.value = value; }

      private static String doInternal() { return "123"; }

      void provideSomeService() {}

      String doSomething(URL url) { return url.toString(); }

      int getValue() { return value; }
      void setValue(int value) { this.value = value; }
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectOnlyOneInvocationOnLocalMockedTypeButExerciseOthersDuringReplay()
   {
      Collaborator collaborator = new Collaborator();

      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService();
         }
      };

      collaborator.provideSomeService();
      collaborator.setValue(1);
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectOnlyOneInvocationOnTestScopedMockedTypeButExerciseOthersDuringReplay(final Collaborator mock)
   {
      new Expectations() {{ mock.provideSomeService(); }};

      mock.provideSomeService();
      mock.setValue(1);
   }

   @Test
   public void recordNothingOnLocalMockedTypeAndExerciseItDuringReplay()
   {
      Collaborator collaborator = new Collaborator();

      new Expectations() { @Mocked Collaborator mock; };

      collaborator.provideSomeService();
   }

   @Test
   public void recordNothingOnTestScopedMockedTypeAndExerciseItDuringReplay(Collaborator mock)
   {
      new Expectations() {};

      mock.provideSomeService();
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectNothingOnLocalMockedTypeButExerciseItDuringReplay()
   {
      Collaborator collaborator = new Collaborator();

      new Expectations() {
         Collaborator mock;

         {
            mock.provideSomeService(); times = 0;
         }
      };

      collaborator.setValue(2);
   }

   @Test(expected = UnexpectedInvocation.class)
   public void expectNothingOnTestScopedMockedTypeButExerciseItDuringReplay(final Collaborator mock)
   {
      new Expectations() {{
         mock.setValue(anyInt); times = 0;
      }};

      mock.setValue(2);
   }

   @Test
   public void mockInterface(final Runnable mock)
   {
      new Expectations() {{ mock.run(); }};

      mock.run();
   }

   public interface IA {}
   public interface IB extends IA {}
   public interface IC { boolean doSomething(IB b); }

   @Test
   public void mockInterfaceWhichExtendsAnother(final IB b, final IC c)
   {
      new Expectations() {{
         c.doSomething(b); result = false;
         invoke(c, "doSomething", b); result = true;
      }};

      assertFalse(c.doSomething(b));
      assertTrue(c.doSomething(b));
   }

   public abstract static class AbstractCollaborator
   {
      String doSomethingConcrete() { return "test"; }
      protected abstract void doSomethingAbstract();
   }

   @Test
   public void mockAbstractClass(final AbstractCollaborator mock)
   {
      new Expectations() {{
         mock.doSomethingConcrete();
         mock.doSomethingAbstract();
      }};

      mock.doSomethingConcrete();
      mock.doSomethingAbstract();
   }

   @Test
   public void mockFinalField()
   {
      new Expectations() {
         final Collaborator mock = new Collaborator();

         {
            mock.getValue();
         }
      };

      new Collaborator().getValue();
   }

   @Test
   public void mockClassWithoutDefaultConstructor()
   {
      new Expectations() { @Mocked Dummy mock; };
   }

   static class Dummy
   {
      @SuppressWarnings("UnusedDeclaration")
      Dummy(int i) {}
   }

   static final class SubCollaborator extends Collaborator
   {
      @Override int getValue() { return 1 + super.getValue(); }
      int getValue(int i) { return i + super.getValue(); }
   }

   @Test
   public void mockSubclass()
   {
      new Expectations() {
         final SubCollaborator mock = new SubCollaborator();

         {
            mock.provideSomeService();
            mock.getValue(); result = 1;
         }
      };

      SubCollaborator collaborator = new SubCollaborator();
      collaborator.provideSomeService();
      assertEquals(1, collaborator.getValue());
   }

   @Test
   public void mockSuperClassUsingLocalMockField()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.getValue(); result = 1;
            mock.getValue(); result = 2;
         }
      };

      SubCollaborator collaborator = new SubCollaborator();
      assertEquals(2, collaborator.getValue());
      assertEquals(3, collaborator.getValue(1));
   }

   @Test
   public void mockSuperClassUsingMockParameter(@NonStrict final Collaborator mock)
   {
      new Expectations() {{
         mock.getValue(); times = 2; returns(1, 2);
      }};

      SubCollaborator collaborator = new SubCollaborator();
      assertEquals(2, collaborator.getValue());
      assertEquals(3, collaborator.getValue(1));
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToRecordExpectedReturnValueForNoCurrentInvocation()
   {
      new Expectations() {
         @Mocked Collaborator mock;

         {
            result = 42;
         }
      };
   }

   @Test(expected = IllegalStateException.class)
   public void attemptToAddArgumentMatcherWhenNotRecording()
   {
      new Expectations() {
         @Mocked Collaborator mock;
      }.withNotEqual(5);
   }

   @Test
   public void mockClassWithMethodsOfAllReturnTypesReturningDefaultValues()
   {
      ClassWithMethodsOfEveryReturnType realObject = new ClassWithMethodsOfEveryReturnType();

      new Expectations() {
         ClassWithMethodsOfEveryReturnType mock;

         {
            mock.getBoolean();
            mock.getChar();
            mock.getByte();
            mock.getShort();
            mock.getInt();
            mock.getLong();
            mock.getFloat();
            mock.getDouble();
            mock.getObject();
            mock.getElements();
         }
      };

      assertFalse(realObject.getBoolean());
      assertEquals('\0', realObject.getChar());
      assertEquals(0, realObject.getByte());
      assertEquals(0, realObject.getShort());
      assertEquals(0, realObject.getInt());
      assertEquals(0L, realObject.getLong());
      assertEquals(0.0, realObject.getFloat(), 0.0);
      assertEquals(0.0, realObject.getDouble(), 0.0);
      assertNull(realObject.getObject());
      assertFalse(realObject.getElements().hasMoreElements());
   }

   static class ClassWithMethodsOfEveryReturnType
   {
      boolean getBoolean() { return true; }
      char getChar() { return 'A' ; }
      byte getByte() { return 1; }
      short getShort() { return 1; }
      int getInt() { return 1; }
      long getLong() { return 1; }
      float getFloat() { return 1.0F; }
      double getDouble() { return 1.0; }
      Object getObject() { return new Object(); }
      Enumeration<?> getElements() { return null; }
   }

   @Test(expected = UnexpectedInvocation.class)
   public void replayWithUnexpectedStaticMethodInvocation()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.getValue();
         }
      };

      Collaborator.doInternal();
   }

   @Test(expected = MissingInvocation.class)
   public void replayWithMissingExpectedMethodInvocation()
   {
      new Expectations() {
         Collaborator mock;

         {
            mock.setValue(123);
         }
      };
   }

   @Test
   public void defineTwoConsecutiveReturnValues(final Collaborator mock)
   {
      new Expectations() {{
         mock.getValue(); result = 1; result = 2;
      }};

      assertEquals(1, mock.getValue());
      assertEquals(2, mock.getValue());
   }

   @Test // Note: this test only works under JDK 1.6+; JDK 1.5 does not support redefining natives.
   public void mockNativeMethod()
   {
      new Expectations() {
         @Mocked final System system = null;

         {
            System.nanoTime(); result = 0L;
         }
      };

      assertEquals(0, System.nanoTime());
   }

   @Test
   public void mockSystemGetenvMethod()
   {
      new Expectations() {
         @Mocked System mockedSystem;

         {
            System.getenv("envVar"); result = ".";
         }
      };

      assertEquals(".", System.getenv("envVar"));
   }

   @Test
   public void mockConstructorsInJREClassHierarchies() throws Exception
   {
      new Expectations() {
         final FileWriter fileWriter;
         @Mocked PrintWriter printWriter;

         {
            fileWriter = new FileWriter("no.file");
         }
      };

      new FileWriter("no.file");
   }

   @Test(expected = UnexpectedInvocation.class)
   public void failureFromUnexpectedInvocationInAnotherThread() throws Exception
   {
      final Collaborator collaborator = new Collaborator();
      Thread t = new Thread() {
         @Override
         public void run() { collaborator.provideSomeService(); }
      };

      new Expectations() {
         Collaborator mock;

         {
            mock.getValue();
         }
      };

      collaborator.getValue();
      t.start();
      t.join();
   }

   public interface InterfaceWithStaticInitializer { Object X = "x"; }

   @Test
   public void mockInterfaceWithStaticInitializer(InterfaceWithStaticInitializer mock)
   {
      assertNotNull(mock);
      assertEquals("x", InterfaceWithStaticInitializer.X);
   }

   @Test
   public void recordStrictExpectationsAllowingZeroInvocationsAndReplayNone(final Collaborator mock)
   {
      new Expectations() {{
         mock.provideSomeService(); minTimes = 0;
         mock.setValue(1); minTimes = 0;
      }};

      // Don't exercise anything.
   }

   @Test
   public void recordingExpectationOnMethodWithOneArgumentButReplayingWithAnotherShouldProduceUsefulErrorMessage(
      final Collaborator mock) throws Exception
   {
      final URL expectedURL = new URL("http://expected");

      new Expectations() {{ mock.doSomething(expectedURL); }};

      mock.doSomething(expectedURL);

      URL anotherURL = new URL("http://another");

      try {
         mock.doSomething(anotherURL);
         fail();
      }
      catch (UnexpectedInvocation e) {
         assertTrue(e.getMessage().contains(anotherURL.toString()));
      }
   }

   @Test
   public void recordExpectationInMethodOfExpectationBlockInsteadOfConstructor(@Mocked final Collaborator mock)
   {
      new Expectations() {
         {
            recordExpectation();
         }

         private void recordExpectation()
         {
            mock.getValue();
            result = 123;
         }
      };

      assertEquals(123, mock.getValue());
   }
}
