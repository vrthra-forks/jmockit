/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package org.easymock.samples;

import java.util.*;

import static org.junit.Assert.*;
import org.junit.*;

import mockit.*;

public final class DocumentManager_JMockit_Test
{
   DocumentManager classUnderTest;
   @Mocked Collaborator mock; // A mock field which will be automatically set.

   @Before
   public void setup()
   {
      classUnderTest = new DocumentManager();
      classUnderTest.addListener(mock);
   }

   @Test
   public void removeNonExistingDocument()
   {
      assertTrue(classUnderTest.removeDocument("Does not exist"));

      // Verify there were no uses of the collaborator.
      new FullVerifications() {};
   }

   @Test
   public void addDocument()
   {
      new Expectations() {{ mock.documentAdded("New Document"); }};

      classUnderTest.addDocument("New Document", new byte[0]);
   }

   @Test
   public void addAndChangeDocument()
   {
      new Expectations() {{
         mock.documentAdded("Document");
         mock.documentChanged("Document"); times = 3;
      }};

      classUnderTest.addDocument("Document", new byte[0]);
      classUnderTest.addDocument("Document", new byte[0]);
      classUnderTest.addDocument("Document", new byte[0]);
      classUnderTest.addDocument("Document", new byte[0]);
   }

   @Test
   public void voteForRemoval()
   {
      new Expectations() {{
         // Expect document addition.
         mock.documentAdded("Document");
         // Expect to be asked to vote, and vote for it.
         mock.voteForRemoval("Document"); result = 42;
         // Expect document removal.
         mock.documentRemoved("Document");
      }};

      classUnderTest.addDocument("Document", new byte[0]);
      assertTrue(classUnderTest.removeDocument("Document"));
   }

   @Test
   public void voteAgainstRemoval()
   {
      new Expectations() {{
         // Expect document addition.
         mock.documentAdded("Document");
         // Expect to be asked to vote, and vote against it.
         mock.voteForRemoval("Document"); result = -42;
         // Document removal is *not* expected.
      }};

      classUnderTest.addDocument("Document", new byte[0]);
      assertFalse(classUnderTest.removeDocument("Document"));
   }

   @Test
   public void voteForRemovals()
   {
      new Expectations() {{
         mock.documentAdded("Document 1");
         mock.documentAdded("Document 2");
         mock.voteForRemovals("Document 1", "Document 2"); result = 42;
         mock.documentRemoved("Document 1");
         mock.documentRemoved("Document 2");
      }};

      classUnderTest.addDocument("Document 1", new byte[0]);
      classUnderTest.addDocument("Document 2", new byte[0]);
      assertTrue(classUnderTest.removeDocuments("Document 1", "Document 2"));
   }

   @Test
   public void voteAgainstRemovals()
   {
      new Expectations() {{
         mock.documentAdded("Document 1");
         mock.documentAdded("Document 2");
         mock.voteForRemovals("Document 1", "Document 2"); result = -42;
      }};

      classUnderTest.addDocument("Document 1", new byte[0]);
      classUnderTest.addDocument("Document 2", new byte[0]);
      assertFalse(classUnderTest.removeDocuments("Document 1", "Document 2"));
   }

   @Test
   public void delegateMethodWhichProducesResultBasedOnCustomLogic(@Mocked final List<String> l)
   {
      new Expectations() {{
         l.remove(10);
         result = new Delegate() {
            String remove(int index) { return String.valueOf(index); }
         };
      }};

      assertEquals("10", l.remove(10));
   }
}
