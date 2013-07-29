/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package orderMngr.service.domain;

import java.io.*;

import orderMngr.service.persistence.*;
import org.junit.*;
import static org.junit.Assert.*;

@Ignore
public class DomainTest
{
   @Before
   public final void setUp()
   {
      Persistence.beginTransaction();
   }

   @After
   public final void tearDown()
   {
      Persistence.persistPendingChanges();
      Persistence.rollbackTransaction();
      Persistence.closeUnitOfWork();
   }

   protected final void assertPersisted(Serializable entityId, Object entity)
   {
      Persistence.unload(entity);
      Object loadedEntity = Persistence.load(entity.getClass(), entityId);
      assertEquals(entity, loadedEntity);
   }

   protected final void assertUpdated(Object entity)
   {
      Persistence.persistPendingChanges();
      assertPersisted(Persistence.entityId(entity), entity);
   }

   protected final void assertDeleted(Object entity)
   {
      assertFalse(
         "Entity " + entity + " not actually deleted from persistent store", 
         Persistence.exists(entity.getClass(), Persistence.entityId(entity)));
   }
}
