/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3;

import java.math.*;
import java.util.*;

import org.hibernate.*;
import static org.junit.Assert.*;
import org.junit.*;

public final class QueryTest
{
   private List<Object> entities;
   private ChildEntity child;

   public static class ParentEntity
   {
      static long nextId = 1;
      
      private long id;
      private int seqNumber;
      private String name;
      private String desc;
      private Date created;
      private Collection<ChildEntity> children = new LinkedList<ChildEntity>();
      
      ParentEntity(int seqNumber, String name)
      {
         id = nextId;
         nextId++;
         this.seqNumber = seqNumber;
         this.name = name;
         created = new Date();
      }

      ParentEntity(long id, int seqNumber, String name, String desc, Date created)
      {
         this.id = id;
         this.seqNumber = seqNumber;
         this.name = name;
         this.desc = desc;
         this.created = created;
      }

      public long getId()
      {
         return id;
      }

      void setId(long id)
      {
         this.id = id;
      }

      public int getSeqNumber()
      {
         return seqNumber;
      }

      void setSeqNumber(int seqNumber)
      {
         this.seqNumber = seqNumber;
      }

      public String getName()
      {
         return name;
      }

      public void setName(String name)
      {
         this.name = name;
      }

      public String getDesc()
      {
         return desc;
      }

      public void setDesc(String desc)
      {
         this.desc = desc;
      }

      public Date getCreated()
      {
         return created;
      }

      void setCreated(Date created)
      {
         this.created = created;
      }

      public Collection<ChildEntity> getChildren()
      {
         return children;
      }
   }

   public static class ChildEntity
   {
      private static int nextId = 1;
      private int id;
      private ParentEntity parent;
      private String code;
      private BigDecimal value;

      ChildEntity(ParentEntity parent, String code, BigDecimal value)
      {
         this(nextId, parent, code, value);
         nextId++;
      }

      ChildEntity(int id, ParentEntity parent, String code, BigDecimal value)
      {
         this.id = id;
         this.parent = parent;
         this.code = code;
         this.value = value;
         parent.getChildren().add(this);
      }

      public int getId()
      {
         return id;
      }

      void setId(int id)
      {
         this.id = id;
      }

      public ParentEntity getParent()
      {
         return parent;
      }

      void setParent(ParentEntity parent)
      {
         this.parent = parent;
      }

      public String getCode()
      {
         return code;
      }

      void setCode(String code)
      {
         this.code = code;
      }

      public BigDecimal getValue()
      {
         return value;
      }

      void setValue(BigDecimal value)
      {
         this.value = value;
      }
   }

   @Before
   public void setUp()
   {
      entities = new ArrayList<Object>();

      entities.add(new ParentEntity(1, "Parent 1"));
      entities.add(new ParentEntity(2, "Parent 2"));

      ParentEntity parent = new ParentEntity(3, "Xyz");
      child = new ChildEntity(parent, "569", new BigDecimal("15.60"));
      entities.add(parent);
   }

   private <E> List<E> find(String hql)
   {
      return (List<E>) new QueryEmul(hql, entities).list();
   }

   @Test
   public void findAllParentEntities()
   {
      List<?> found = find("from ParentEntity p");
      assertEquals(entities, found);
   }

   @Test
   public void findBySeqNumbersInGivenList()
   {
      List<ParentEntity> found = find("select e from ParentEntity e where e.seqNumber in (2, 3)");

      for (ParentEntity e : found) {
         assertTrue(e.getSeqNumber() == 2 || e.getSeqNumber() == 3);
      }
   }

   @Test
   public void findAllParentEntitiesWithChildren()
   {
      List<ParentEntity> found = find("select p from ParentEntity p where p.children is not empty");
      assertEquals(1, found.size());
      assertEquals(3, found.get(0).getSeqNumber());

      found = find("select p from ParentEntity p join p.children c");
      assertEquals(1, found.size());
      assertEquals(3, found.get(0).getSeqNumber());
   }

   @Test
   public void findAllChildrenEntities()
   {
      List<ChildEntity> found = find("select c from ParentEntity p join p.children c");

      assertEquals(1, found.size());
      assertEquals("569", found.get(0).getCode());
   }

   @Test
   public void findChildrenEntitiesWithGivenCode()
   {
      ParentEntity parent = new ParentEntity(4, "With 2 children");
      new ChildEntity(parent, "C1", null);
      ChildEntity child = new ChildEntity(parent, "C2", null);
      entities.add(parent);

      List<ChildEntity> found =
         find("select c from ParentEntity p join p.children c where c.code='C2'");

      assertEquals(1, found.size());
      assertTrue(found.contains(child));
   }

   @Test
   public void findChildrenEntitiesWithMaxValue()
   {
      addChildrenEntities();
      QueryEmul query = new QueryEmul("from ChildEntity c where c.value < 20.0", entities);

      Iterator<ChildEntity> found = (Iterator<ChildEntity>) query.iterate();

      assertSame(child, found.next());
   }

   private void addChildrenEntities()
   {
      entities.add(child);
   }

   @Test
   public void findChildrenEntitiesWithGivenParentName()
   {
      addChildrenEntities();
      QueryEmul query = new QueryEmul("select c from ChildEntity c where c.parent.name like ?", entities);
      query.setParameter(0, "%y%");

      ScrollableResults found = query.scroll();

      assertTrue(found.next());
      assertSame(child, found.get(0));
   }

   @Test
   public void findChildrenAndParentEntitiesWithGivenParentSeqNumber()
   {
      addChildrenEntities();
      QueryEmul query = new QueryEmul("select c, p from ChildEntity c join c.parent p where p.seqNumber=3", entities);

      List<?> found = query.list();

      assertEquals(1, found.size());
      Object[] result = (Object[]) found.get(0);
      assertSame(child, result[0]);
      assertSame(child.getParent(), result[1]);
   }
}