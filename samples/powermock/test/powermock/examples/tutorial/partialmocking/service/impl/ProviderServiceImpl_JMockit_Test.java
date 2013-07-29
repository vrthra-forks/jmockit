/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package powermock.examples.tutorial.partialmocking.service.impl;

import java.util.*;

import org.junit.*;

import mockit.*;

import static org.junit.Assert.*;
import powermock.examples.tutorial.partialmocking.dao.*;
import powermock.examples.tutorial.partialmocking.dao.domain.impl.*;
import powermock.examples.tutorial.partialmocking.domain.*;
import powermock.examples.tutorial.partialmocking.service.*;

/**
 * Demonstrates <em>dynamic partial mocking</em>, where the methods to mock are determined from
 * those actually called in the record phase. In contrast, when using regular/static partial
 * mocking, the names of the desired methods need to be individually specified in strings.
 * <p/>
 * The first four tests mock a private method defined in the class under test, while the last two
 * tests directly exercise this private method. This is not recommended, though. Instead, unit tests
 * should be created only for the non-private methods in the class under test.
 * <p/>
 * <a href="http://code.google.com/p/powermock/source/browse/trunk/examples/tutorial/src/solution/java/demo/org/powermock/examples/tutorial/partialmocking/service/impl/ProviderServiceImplTest.java">PowerMock version</a>
 */
public final class ProviderServiceImpl_JMockit_Test
{
   @Test
   public void testGetAllServiceProviders()
   {
      final Set<ServiceProducer> expectedServiceProducers = new HashSet<ServiceProducer>();
      expectedServiceProducers.add(new ServiceProducer(1, "mock name"));

      final ProviderService tested = new ProviderServiceImpl();

      new Expectations(tested) {{
         invoke(tested, "getAllServiceProducers"); result = expectedServiceProducers;
      }};

      Set<ServiceProducer> actualServiceProviders = tested.getAllServiceProviders();

      assertSame(expectedServiceProducers, actualServiceProviders);
   }

   @Test
   public void testGetAllServiceProviders_noServiceProvidersFound()
   {
      Set<ServiceProducer> expectedServiceProducers = new HashSet<ServiceProducer>();
      final ProviderService tested = new ProviderServiceImpl();

      new Expectations(tested) {{ invoke(tested, "getAllServiceProducers"); result = null; }};

      Set<ServiceProducer> actualServiceProviders = tested.getAllServiceProviders();

      assertNotSame(expectedServiceProducers, actualServiceProviders);
      assertEquals(expectedServiceProducers, actualServiceProviders);
   }

   @Test
   public void testGetServiceProvider_found()
   {
      int expectedServiceProducerId = 1;
      ServiceProducer expected = new ServiceProducer(expectedServiceProducerId, "mock name");

      final Set<ServiceProducer> serviceProducers = new HashSet<ServiceProducer>();
      serviceProducers.add(expected);

      final ProviderService tested = new ProviderServiceImpl();

      new Expectations(tested) {{ invoke(tested, "getAllServiceProducers"); result = serviceProducers; }};

      ServiceProducer actual = tested.getServiceProvider(expectedServiceProducerId);

      assertSame(expected, actual);
   }

   @Test
   public void testGetServiceProvider_notFound()
   {
      final ProviderService tested = new ProviderServiceImpl();

      new Expectations(tested) {{
         invoke(tested, "getAllServiceProducers");
         // An empty collection is the default return value, so we don't have to record it here.
         // returns(new HashSet<ServiceProducer>());
      }};

      ServiceProducer actual = tested.getServiceProvider(1);

      assertNull(actual);
   }

   // A reusable Expectations subclass is defined here, declaring a common mock field for use in a
   // subset of the tests in this class.
   // A better alternative would be to separate this test class in two, one for each set of mock
   // fields and corresponding set of tests. The advantage then would be that each test class could
   // define common fields and methods at the class level.

   static class RealProviderServiceExpectations extends Expectations
   {
      private final ProviderService providerService;
      @Mocked protected ProviderDao providerDao;

      {
         providerService = new ProviderServiceImpl();
         setField(providerService, providerDao);
      }

      protected Set<ServiceProducer> getAllServiceProducers()
      {
         return invoke(providerService, "getAllServiceProducers");
      }
   }

   @Test
   public void getAllServiceProducers()
   {
      String expectedName = "mock name";
      int expectedId = 1;

      final Set<ServiceArtifact> serviceArtifacts = new HashSet<ServiceArtifact>();
      serviceArtifacts.add(new ServiceArtifact(expectedId, expectedName));

      RealProviderServiceExpectations expectations = new RealProviderServiceExpectations() {{
         providerDao.getAllServiceProducers();
         result = serviceArtifacts;
      }};

      Set<ServiceProducer> allProducers = expectations.getAllServiceProducers();

      assertEquals(1, allProducers.size());
      assertTrue(allProducers.contains(new ServiceProducer(expectedId, expectedName)));
   }

   @Test
   public void getAllServiceProducersOnEmptyProviderService()
   {
      RealProviderServiceExpectations expectations = new RealProviderServiceExpectations() {{
         providerDao.getAllServiceProducers(); result = new HashSet<ServiceArtifact>();
      }};

      Set<ServiceProducer> allProducers = expectations.getAllServiceProducers();

      assertTrue(allProducers.isEmpty());
   }
}
