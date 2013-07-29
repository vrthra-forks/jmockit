/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit;

import java.io.*;
import java.net.*;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.*;

import org.junit.*;

import static org.junit.Assert.*;

import mockit.internal.*;

public final class CascadingParametersAndLocalFieldsTest
{
   static class Foo
   {
      Bar getBar() { return null; }

      static Bar globalBar() { return null; }

      void doSomething(String s) { throw new RuntimeException(s); }
      int getIntValue() { return 1; }
      private Boolean getBooleanValue() { return true; }
      final List<Integer> getList() { return null; }
      Callable<?> returnTypeWithWildcard() { return null; }
      <RT extends Baz> RT returnTypeWithBoundedTypeVariable() { return null; }
   }

   static class Bar
   {
      Bar() { throw new RuntimeException(); }
      int doSomething() { return 1; }
      Baz getBaz() { return null; }
      AnEnum getEnum() { return null; }
      static String staticMethod() { return "notMocked"; }
   }

   public interface Baz
   {
      void runIt();
      Date getDate();
   }

   enum AnEnum { First, Second, Third }

   @Test
   public void cascadeOneLevelDuringReplay(@Cascading Foo foo)
   {
      assertEquals(0, foo.getBar().doSomething());
      assertEquals(0, Foo.globalBar().doSomething());
      assertNotSame(foo.getBar(), Foo.globalBar());

      foo.doSomething("test");
      assertEquals(0, foo.getIntValue());
      assertNull(foo.getBooleanValue());
      assertTrue(foo.getList().isEmpty());
      assertNotNull(foo.returnTypeWithWildcard());
      assertNotNull(foo.returnTypeWithBoundedTypeVariable());
   }

   @Test
   public void verifyThatAllCascadedInstancesHaveBeenDiscarded(Foo foo)
   {
      assertNull(foo.getBar());
   }

   @Test
   public void verifyThatStaticMethodsAndConstructorsAreNotMockedWhenCascading(@Cascading Foo foo)
   {
      foo.getBar();
      
      assertEquals("notMocked", Bar.staticMethod());
      
      try {
         new Bar();
         fail();
      }
      catch (RuntimeException ignored) {}
   }

   @Test
   public void verifyThatStaticMethodsAndConstructorsAreMockedWhenCascadedMockIsMockedNormally(
      @Cascading Foo mockFoo, @Mocked Bar mockBar)
   {
      assertSame(mockBar, mockFoo.getBar());
      assertEquals(0, mockBar.doSomething());
      assertNull(Bar.staticMethod());
      new Bar();
   }

   @Test
   public void cascadeOneLevelDuringRecord(final Callable<String> action)
   {
      final List<Integer> list = Arrays.asList(1, 2, 3);

      new NonStrictExpectations() {
         @Cascading Foo foo;

         {
            foo.doSomething(anyString); minTimes = 2;
            foo.getBar().doSomething(); result = 2;
            Foo.globalBar().doSomething(); result = 3;
            foo.getBooleanValue(); result = true;
            foo.getIntValue(); result = -1;
            foo.getList(); result = list;
            foo.returnTypeWithWildcard(); result = action;
         }
      };

      Foo foo = new Foo();
      foo.doSomething("1");
      assertEquals(2, foo.getBar().doSomething());
      foo.doSomething("2");
      assertEquals(3, Foo.globalBar().doSomething());
      assertTrue(foo.getBooleanValue());
      assertEquals(-1, foo.getIntValue());
      assertSame(list, foo.getList());
      assertSame(action, foo.returnTypeWithWildcard());
   }

   @Test
   public void cascadeOneLevelDuringVerify(@Cascading final Foo foo)
   {
      Bar bar = foo.getBar();
      bar.doSomething();
      bar.doSomething();

      Foo.globalBar().doSomething();

      assertEquals(0, foo.getIntValue());
      assertNull(foo.getBooleanValue());

      assertTrue(foo.getList().isEmpty());

      new Verifications() {{
         foo.getBar().doSomething(); minTimes = 2;
         Foo.globalBar().doSomething(); times = 1;
      }};

      new VerificationsInOrder() {{
         foo.getIntValue();
         foo.getBooleanValue();
      }};
   }

   @Test
   public void cascadeTwoLevelsDuringReplay(@Cascading Foo foo)
   {
      foo.getBar().getBaz().runIt();
   }

   @Test
   public void cascadeTwoLevelsDuringRecord()
   {
      final Date now = new Date();

      new Expectations() {
         @Cascading @Mocked final Foo foo = new Foo();

         {
            foo.getBar().doSomething(); result = 1;
            Foo.globalBar().doSomething(); result = 2;

            foo.getBar().getBaz().runIt(); times = 2;

            foo.returnTypeWithBoundedTypeVariable().getDate(); result = now;
         }
      };

      Foo foo = new Foo();
      assertEquals(1, foo.getBar().doSomething());
      assertEquals(2, Foo.globalBar().doSomething());

      Baz baz = foo.getBar().getBaz();
      baz.runIt();
      baz.runIt();

      assertSame(now, foo.returnTypeWithBoundedTypeVariable().getDate());
   }

   static class GenericFoo<T, U extends Bar>
   {
      T returnTypeWithUnboundedTypeVariable() { return null; }
      U returnTypeWithBoundedTypeVariable() { return null; }
   }
   static final class SubBar extends Bar {}

   @Test
   public void cascadeGenericMethods(@Cascading GenericFoo<Baz, SubBar> foo)
   {
      Baz t = foo.returnTypeWithUnboundedTypeVariable();
      assertNotNull(t);

      SubBar u = foo.returnTypeWithBoundedTypeVariable();
      assertNotNull(u);
   }

   @Test
   public void cascadeOneLevelAndVerifyInvocationOnLastMockOnly(@Cascading Foo foo, @Injectable final Bar bar)
   {
      Bar fooBar = foo.getBar();
      assertSame(bar, fooBar);
      fooBar.doSomething();

      new Verifications() {{ bar.doSomething(); }};
   }

   @Test
   public void cascadeTwoLevelsWithInvocationRecordedOnLastMockOnly(@Cascading Foo foo)
   {
      new Expectations() {
         @NonStrict Baz baz;

         {
            baz.runIt(); times = 1;
         }
      };

      Baz cascadedBaz = foo.getBar().getBaz();
      cascadedBaz.runIt();
   }

   @Test
   public void cascadeTwoLevelsAndVerifyInvocationOnLastMockOnly(@Cascading Foo foo, final Baz baz)
   {
      Baz cascadedBaz = foo.getBar().getBaz();
      assertSame(baz, cascadedBaz);
      cascadedBaz.runIt();

      new Verifications() {{ baz.runIt(); }};
   }

   // Tests using the java.lang.Process and java.lang.ProcessBuilder classes //////////////////////////////////////////

   @Test
   public void cascadeOnJREClasses() throws Exception
   {
      new NonStrictExpectations() {
         @Cascading ProcessBuilder pb;

         {
            ProcessBuilder sameBuilder = pb.directory((File) any);
            assertSame(sameBuilder, pb);

            Process process = sameBuilder.start();
            process.getOutputStream().write(5);
            process.exitValue(); result = 1;
         }
      };

      Process process = new ProcessBuilder("test").directory(new File("myDir")).start();
      process.getOutputStream().write(5);
      process.getOutputStream().flush();
      assertEquals(1, process.exitValue());
   }

   // Tests using java.net classes ////////////////////////////////////////////////////////////////////////////////////

   static final class SocketFactory
   {
      public Socket createSocket() { return new Socket(); }

      public Socket createSocket(String host, int port) throws IOException
      {
         return new Socket(host, port);
      }
   }

   @Test
   public void mockThroughFinalMockFieldAClassToBeLaterMockedThroughCascading()
   {
      new NonStrictExpectations() {
         // This caused a NPE in later tests which cascade-mocked the Socket class:
         @Mocked final Socket s = null;
      };
   }

   @Test
   public void cascadeOneLevelWithArgumentMatchers(@Cascading final SocketFactory sf) throws Exception
   {
      new NonStrictExpectations() {{
         sf.createSocket(anyString, 80); result = null;
      }};

      assertNull(sf.createSocket("expected", 80));
      assertNotNull(sf.createSocket("unexpected", 8080));
   }

   @Test
   public void recordAndVerifyOneLevelDeep(@Cascading final SocketFactory sf) throws Exception
   {
      final OutputStream out = new ByteArrayOutputStream();

      new NonStrictExpectations() {{
         sf.createSocket().getOutputStream(); result = out;
      }};

      assertSame(out, sf.createSocket().getOutputStream());

      new FullVerifications() {{ sf.createSocket().getOutputStream(); }};
   }

   @Test
   public void recordAndVerifyOnTwoCascadingMocksOfTheSameType(
      @Cascading final SocketFactory sf1, @Cascading final SocketFactory sf2) throws Exception
   {
      final OutputStream out1 = new ByteArrayOutputStream();
      final OutputStream out2 = new ByteArrayOutputStream();

      new NonStrictExpectations() {{
         sf1.createSocket().getOutputStream(); result = out1;
         sf2.createSocket().getOutputStream(); result = out2;
      }};

      assertSame(out1, sf1.createSocket().getOutputStream());
      assertSame(out2, sf2.createSocket().getOutputStream());

      new FullVerificationsInOrder() {{
         sf1.createSocket().getOutputStream();
         sf2.createSocket().getOutputStream();
      }};
   }

   @Test
   public void recordAndVerifySameInvocationOnMocksReturnedFromInvocationsWithDifferentArguments(
      @Cascading final SocketFactory sf) throws Exception
   {
      new NonStrictExpectations() {{
         sf.createSocket().getPort(); result = 1;
         sf.createSocket("first", 80).getPort(); result = 2;
         sf.createSocket("second", 80).getPort(); result = 3;
         sf.createSocket(anyString, 81).getPort(); result = 4;
      }};

      assertEquals(1, sf.createSocket().getPort());
      assertEquals(2, sf.createSocket("first", 80).getPort());
      assertEquals(3, sf.createSocket("second", 80).getPort());
      assertEquals(4, sf.createSocket("third", 81).getPort());

      new Verifications() {{
         sf.createSocket("first", 80).getPort();
         sf.createSocket().getPort(); times = 1;
         sf.createSocket(anyString, 81).getPort(); maxTimes = 1;
         sf.createSocket("second", 80).getPort();
         sf.createSocket("fourth", -1); times = 0;
      }};
   }

   @Test
   public void cascadeOnInheritedMethod(@Cascading SocketChannel sc)
   {
      assertNotNull(sc.provider());
   }

   @Test
   public void recordAndVerifyWithMixedCascadeLevels(@Cascading final SocketFactory sf) throws Exception
   {
      new NonStrictExpectations() {{
         sf.createSocket("first", 80).getKeepAlive(); result = true;
         sf.createSocket("second", anyInt).getChannel().close(); times = 1;
      }};

      sf.createSocket("second", 80).getChannel().close();
      assertTrue(sf.createSocket("first", 80).getKeepAlive());
      sf.createSocket("first", 8080).getChannel().provider().openPipe();

      new Verifications() {{
         sf.createSocket("first", 8080).getChannel().provider().openPipe();
      }};
   }

   @Test
   public void overrideCascadedMockAndRecordStrictExpectationOnIt(@Cascading final Foo foo, final Bar mockBar)
   {
      new Expectations() {{
         foo.getBar(); result = mockBar;
         mockBar.doSomething();
      }};

      Bar bar = foo.getBar();
      bar.doSomething();
   }

   @Test
   public void overrideCascadedMockAndRecordNonStrictExpectationOnIt(@Cascading final Foo foo)
   {
      new NonStrictExpectations() {
         Bar mockBar;

         {
            foo.getBar(); result = mockBar;
            mockBar.doSomething(); times = 1; result = 123;
         }
      };

      Bar bar = foo.getBar();
      assertEquals(123, bar.doSomething());
   }

   @Test
   public void overrideTwoCascadedMocksOfTheSameType(@Cascading final Foo foo1, @Cascading final Foo foo2)
   {
      new Expectations() {
         Bar bar1;
         Bar bar2;

         {
            foo1.getBar(); result = bar1;
            foo2.getBar(); result = bar2;
            bar1.doSomething();
            bar2.doSomething();
         }
      };

      Bar bar1 = foo1.getBar();
      Bar bar2 = foo2.getBar();
      bar1.doSomething();
      bar2.doSomething();
   }

   @Test(expected = UnexpectedInvocation.class)
   public void overrideTwoCascadedMocksOfTheSameTypeButReplayInDifferentOrder(
      @Cascading final Foo foo1, @Cascading final Foo foo2)
   {
      new Expectations() {
         Bar bar1;
         Bar bar2;

         {
            foo1.getBar(); result = bar1;
            foo2.getBar(); result = bar2;
            bar1.doSomething();
            bar2.doSomething();
         }
      };

      Bar bar1 = foo1.getBar();
      Bar bar2 = foo2.getBar();
      bar2.doSomething();
      bar1.doSomething();
   }

   @Test
   public void cascadedEnum(@Cascading final Foo mock)
   {
      new Expectations() {{
         mock.getBar().getEnum(); result = AnEnum.Second;
      }};

      assertEquals(AnEnum.Second, mock.getBar().getEnum());
   }

   @Test
   public void cascadedNonStrictEnumReturningConsecutiveValuesThroughResultField(@Cascading final Foo mock)
   {
      new NonStrictExpectations() {{
         mock.getBar().getEnum();
         result = AnEnum.First;
         result = AnEnum.Second;
         result = AnEnum.Third;
      }};

      assertSame(AnEnum.First, mock.getBar().getEnum());
      assertSame(AnEnum.Second, mock.getBar().getEnum());
      assertSame(AnEnum.Third, mock.getBar().getEnum());
   }

   @Test
   public void cascadedNonStrictEnumReturningConsecutiveValuesThroughReturnsMethod(@NonStrict @Cascading final Foo mock)
   {
      new Expectations() {{
         mock.getBar().getEnum();
         returns(AnEnum.First, AnEnum.Second, AnEnum.Third);
      }};

      assertSame(AnEnum.First, mock.getBar().getEnum());
      assertSame(AnEnum.Second, mock.getBar().getEnum());
      assertSame(AnEnum.Third, mock.getBar().getEnum());
   }

   @Test
   public void cascadedStrictEnumReturningConsecutiveValuesThroughResultField(@Cascading final Foo mock)
   {
      new Expectations() {{
         mock.getBar().getEnum();
         result = AnEnum.Third;
         result = AnEnum.Second;
         result = AnEnum.First;
      }};

      Bar bar = mock.getBar();
      assertSame(AnEnum.Third, bar.getEnum());
      assertSame(AnEnum.Second, bar.getEnum());
      assertSame(AnEnum.First, bar.getEnum());
   }

   @Test
   public void cascadedStrictEnumReturningConsecutiveValuesThroughReturnsMethod(@Cascading final Foo mock)
   {
      new Expectations() {{
         mock.getBar().getEnum();
         returns(AnEnum.First, AnEnum.Second, AnEnum.Third);
      }};

      Bar bar = mock.getBar();
      assertSame(AnEnum.First, bar.getEnum());
      assertSame(AnEnum.Second, bar.getEnum());
      assertSame(AnEnum.Third, bar.getEnum());
   }

   @Test
   public void overrideLastCascadedObjectWithNonMockedInstance()
   {
      final Date newDate = new Date(123);
      assertEquals(123, newDate.getTime());

      new NonStrictExpectations() {
         @Cascading Foo foo;

         {
            foo.getBar().getBaz().getDate();
            result = newDate;
         }
      };

      assertSame(newDate, new Foo().getBar().getBaz().getDate());
      assertEquals(123, newDate.getTime());
   }

   @Test
   public void overrideLastCascadedObjectWithMockedInstance(final Date mockedDate)
   {
      Date newDate = new Date(123);
      assertEquals(0, newDate.getTime());

      new NonStrictExpectations() {
         @Cascading Foo foo;

         {
            foo.getBar().getBaz().getDate();
            result = mockedDate;
         }
      };

      assertSame(mockedDate, new Foo().getBar().getBaz().getDate());
      assertEquals(0, newDate.getTime());
      assertEquals(0, mockedDate.getTime());
   }

   @Test
   public void overrideLastCascadedObjectWithInjectableMockInstance(@Injectable final Date mockDate)
   {
      Date newDate = new Date(123);
      assertEquals(123, newDate.getTime());

      new NonStrictExpectations() {
         @Cascading Foo foo;

         {
            foo.getBar().getBaz().getDate();
            result = mockDate;
         }
      };

      assertSame(mockDate, new Foo().getBar().getBaz().getDate());
      assertEquals(123, newDate.getTime());
      assertEquals(0, mockDate.getTime());
   }

   static class A { B<?> getB() { return null; } }
   static class B<T> { T getValue() { return null; } }

   @Test
   public void cascadeOnMethodReturningAParameterizedClassWithAGenericMethod(@Cascading final A a)
   {
      new NonStrictExpectations() {{
         a.getB().getValue(); result = "test";
      }};

      assertEquals("test", a.getB().getValue());
   }
}
