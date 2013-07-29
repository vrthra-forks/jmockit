/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast.fromClause;

import java.util.*;

import mockit.emulation.hibernate3.ast.*;

public final class FromClause
{
   final List<FromClassOrOuterQueryPath> ranges;

   private FromClause(List<FromClassOrOuterQueryPath> ranges)
   {
      this.ranges = ranges;
   }

   public static FromClause parse(Tokens tokens)
   {
      tokens.next("from");

      List<FromClassOrOuterQueryPath> ranges = new LinkedList<FromClassOrOuterQueryPath>();
      FromClassOrOuterQueryPath range = FromClassOrOuterQueryPath.parse(tokens);
      ranges.add(range);

      while (tokens.hasNext()) {
         char nextToken = tokens.nextChar();

         if (nextToken == ',') {
            range = FromClassOrOuterQueryPath.parse(tokens);
            ranges.add(range);
         }
         else if (!parseJoins(range, tokens)) break;
      }

      return new FromClause(ranges);
   }

   private static boolean parseJoins(FromClassOrOuterQueryPath range, Tokens tokens)
   {
      tokens.pushback();

      FromJoin join = FromJoin.parse(tokens);
      if (join == null) return false;

      range.join = join;

      while (tokens.hasNext() && join != null) {
         join.nextJoin = FromJoin.parse(tokens);
         join = join.nextJoin;
      }
      
      return true;
   }

   public List<Object[]> matches(Collection<?> entities)
   {
      buildAllResultListsFromKnownEntities(entities);
      List<Object[]> tuples = createListOfTuples();
      fillRangeResultValues(tuples, 0, 0, 0);
      return tuples;
   }

   private int fillRangeResultValues(List<Object[]> tuples, int rangeIndex, int initialRow, int j)
   {
      FromClassOrOuterQueryPath range = ranges.get(rangeIndex);
      int i = initialRow;
      Object[] previousTuple = null;

      for (Object value : range.result) {
         Object[] tuple = tuples.get(i);
         tuple[j] = value;

         if (rangeIndex > 0) {
            if (previousTuple != null) {
               copyRepeatValues(previousTuple, tuple, j);
            }

            previousTuple = tuple;
         }

         FromJoin join = range.join;

         if (join == null) {
            i++;
         }
         else {
            i = fillJoinResultValues(tuples, join, value, i, j);
         }

         if (rangeIndex + 1 < ranges.size()) {
            i = fillRangeResultValues(tuples, rangeIndex + 1, i, j + 1);
         }
      }

      return i;
   }

   private int fillJoinResultValues(
      List<Object[]> tuples, FromJoin join, Object parentValue, int initialRow, int j)
   {
      int i = initialRow;
      int jj = j + 1;
      List<Object> joinedValues = join.result.get(parentValue);
      Object[] previousTuple = null;

      for (Object joinedValue : joinedValues) {
         Object[] tuple = tuples.get(i);
         tuple[jj] = joinedValue;

         if (previousTuple != null) {
            copyRepeatValues(previousTuple, tuple, j);
         }

         previousTuple = tuple;

         if (join.nextJoin == null) {
            i++;
         }
         else {
            i = fillJoinResultValues(tuples, join.nextJoin, joinedValue, i, jj);
         }
      }

      return i;
   }

   private void copyRepeatValues(Object[] previousTuple, Object[] tuple, int j)
   {
      for (int jj = 0; jj <= j; jj++) {
         tuple[jj] = previousTuple[jj];
      }
   }

   private void buildAllResultListsFromKnownEntities(Collection<?> entities)
   {
      for (FromClassOrOuterQueryPath range : ranges) {
         range.matches(entities);
      }
   }

   private List<Object[]> createListOfTuples()
   {
      int columns = 0;
      int rows = 1;

      for (FromClassOrOuterQueryPath range : ranges) {
         columns += range.depth();
         rows *= range.tupleCount();
      }

      return listOfEmptyTuples(rows, columns);
   }

   private List<Object[]> listOfEmptyTuples(int rows, int columns)
   {
      List<Object[]> tuples = new ArrayList<Object[]>(rows);

      for (int i = 0; i < rows; i++) {
         tuples.add(new Object[columns]);
      }

      return tuples;
   }

   public int columnIndex(String alias)
   {
      for (FromClassOrOuterQueryPath range : ranges) {
         int index = range.columnIndex(alias);

         if (index >= 0) {
            return index;
         }
      }

      throw new RuntimeException("Invalid alias \"" + alias + "\"");
   }

   public void getAliases(Map<String, Object> aliasToValue)
   {
      for (FromClassOrOuterQueryPath range : ranges) {
         range.getAliases(aliasToValue);
      }
   }
}