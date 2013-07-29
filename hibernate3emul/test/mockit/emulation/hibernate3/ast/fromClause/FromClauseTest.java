/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.fromClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;
import static mockit.emulation.hibernate3.ast.fromClause.FromJoin.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class FromClauseTest
{
   private List<Object[]> tuples;

   @Test(expected = QuerySyntaxException.class)
   public void parseIncompleteClause()
   {
      FromClause.parse(new Tokens("from "));
   }

   public static class Entity
   {
      Entity parent;
      List<Object> children = new LinkedList<Object>();

      Entity(Entity parent) { this.parent = parent; }
      public Entity getParent() { return parent; }
      public List<?> getChildren() { return children; }
   }

   @Test(expected = RuntimeException.class)
   public void columnIndexForUnknownAlias()
   {
      FromClause from = FromClause.parse(new Tokens("from Entity e"));
      matches(from, new Entity(null));
      from.columnIndex("x");
   }

   private void matches(FromClause from, Object... entities)
   {
      tuples = from.matches(Arrays.asList(entities));
   }

   @Test(expected = RuntimeException.class)
   public void columnIndexForUnknownPathElement()
   {
      FromClause from = FromClause.parse(new Tokens("from Entity e join x.parent p"));
      matches(from, new Entity(null));
      from.columnIndex("e");
   }

   @Test
   public void parseAndMatchBasicClause()
   {
      String hql = "from Entity e";
      FromClause from = FromClause.parse(new Tokens(hql));

      assertEquals(1, from.ranges.size());
      FromClassOrOuterQueryPath range = from.ranges.get(0);
      assertFalse(range.isFQName);
      assertEquals("Entity", range.entityClassName);
      assertEquals("e", range.alias);
      assertNull(range.join);

      assertFound(from, "e", new Entity(null));
      assertEquals(1, tuples.get(0).length);

      from = FromClause.parse(new Tokens(hql));
      assertNotFound(from, "e", new Object());
   }

   private void assertFound(FromClause from, String alias, Object entity)
   {
      matches(from, entity);
      assertEquals(1, tuples.size());
      assertEquals(0, from.columnIndex(alias));
      assertSame(entity, tuples.get(0)[0]);
   }

   private void assertNotFound(FromClause from, String alias, Object entity)
   {
      matches(from, entity);
      assertEquals(0, from.columnIndex(alias));
      assertEquals(0, tuples.size());
   }

   @Test
   public void parseAndMatchWithJoin()
   {
      FromClause from = FromClause.parse(new Tokens("from Entity e join e.parent p"));

      assertEquals(1, from.ranges.size());
      FromClassOrOuterQueryPath range = from.ranges.get(0);
      assertFalse(range.isFQName);
      assertEquals("Entity", range.entityClassName);
      assertEquals("e", range.alias);

      FromJoin join = range.join;
      assertNotNull(join);
      assertSame(JoinType.Inner, join.type);
      assertEquals("p", join.pathAndAlias.alias);
      assertEquals("e", join.pathAndAlias.pathElements[0]);
      assertEquals("parent", join.pathAndAlias.pathElements[1]);
      assertNull(join.nextJoin);

      assertNotFound(from, "e", new Entity(null));

      assertFound(from, "e", new Entity(new Entity(null)));
      assertEquals(2, tuples.get(0).length);
   }

   @Ignore @Test
   public void parseAndMatchWithTwoRanges()
   {
      String hql = "from Entity e1, java.lang.Object e2";
      FromClause from = FromClause.parse(new Tokens(hql));

      assertEquals(2, from.ranges.size());
      FromClassOrOuterQueryPath range1 = from.ranges.get(0);
      assertEquals("e1", range1.alias);
      FromClassOrOuterQueryPath range2 = from.ranges.get(1);
      assertTrue(range2.isFQName);
      assertEquals("e2", range2.alias);
      assertEquals("java.lang.Object", range2.entityClassName);

      Object e1 = new Entity(null);
      Object e2 = new Object();
      matches(from, e1, e2);
      assertSame(e1, tuples.get(0)[0]);
      assertSame(e2, tuples.get(0)[1]);

      from = FromClause.parse(new Tokens(hql));
      matches(from, e1);
      assertEquals(0, tuples.size());

      from = FromClause.parse(new Tokens(hql));
      matches(from, e2);
      assertEquals(0, tuples.size());

      from = FromClause.parse(new Tokens(hql));
      matches(from, "");
      assertEquals(0, tuples.size());
   }

   @Test
   public void parseAndMatchWithTwoInnerJoins()
   {
      String hql = "from Entity e join e.parent p join p.parent p2";
      FromClause from = FromClause.parse(new Tokens(hql));

      assertEquals(1, from.ranges.size());

      FromJoin join1 = from.ranges.get(0).join;
      assertNotNull(join1);
      assertNotNull(join1.nextJoin);

      Entity e = new Entity(null);
      matches(from, e);
      assertEquals(0, tuples.size());

      from = FromClause.parse(new Tokens(hql));
      e = new Entity(new Entity(null));
      matches(from, e);
      assertEquals(0, tuples.size());

      from = FromClause.parse(new Tokens(hql));
      Entity p2 = new Entity(null);
      Entity p = new Entity(p2);
      e = new Entity(p);
      matches(from, e);
      Object[] tuple = tuples.get(0);
      assertSame(e, tuple[0]);
      assertSame(p, tuple[1]);
      assertSame(p2, tuple[2]);
   }

   @Test
   public void parseAndMatchWithInnerJoinOnCollection()
   {
      FromClause from = FromClause.parse(new Tokens("from Entity e inner join e.children c"));

      assertEquals(1, from.ranges.size());

      Entity e = new Entity(null);
      matches(from, e);
      assertEquals(0, tuples.size());

      e.children = null;
      matches(from, e);
      assertEquals(0, tuples.size());

      e = new Entity(null);
      Object c = new Object();
      e.children.add(c);
      matches(from, e);
      assertSame(e, tuples.get(0)[0]);
      assertSame(c, tuples.get(0)[1]);

      Object c2 = new Object();
      e.children.add(c2);
      matches(from, e);
      assertSame(e, tuples.get(0)[0]);
      assertSame(c, tuples.get(0)[1]);
      assertSame(e, tuples.get(1)[0]);
      assertSame(c2, tuples.get(1)[1]);
   }

   @Test
   public void matchWithJoinOnCollectionFollowedByOtherJoin()
   {
      FromClause from =
         FromClause.parse(new Tokens("from Entity e join e.children as c join c.parent AS p"));

      Entity entity = new Entity(new Entity(null));
      Entity parent = new Entity(null);
      entity.children.add(new Entity(parent));
      matches(from, entity);
      Object[] tuple = tuples.get(0);
      assertSame(entity, tuple[0]);
      assertSame(entity.children.get(0), tuple[1]);
      assertSame(parent, tuple[2]);
   }
}