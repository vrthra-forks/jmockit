/*
 * Copyright (c) 2006-2011 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.emulation.hibernate3.ast;

import java.util.*;

import mockit.emulation.hibernate3.ast.fromClause.*;
import mockit.emulation.hibernate3.ast.whereClause.*;

/**
 * Abstract Syntax Tree and "Interpreter" (from the GoF Design Patterns) for HQL query statements.
 */
public final class QueryAST
{
   private final Map<Object, Object> parameters;
   private final SelectClause select;
   private final FromClause from;
   private final Expr where;

   public QueryAST(String ql)
   {
      parameters = new HashMap<Object, Object>();

      Tokens tokens = new Tokens(ql);
      select = SelectClause.parse(tokens);

      from = FromClause.parse(tokens);

      if (tokens.hasNext() && "where".equalsIgnoreCase(tokens.next())) {
         where = Expr.parse(tokens);
      }
      else {
         where = null;
      }
   }

   public void setParameter(int position, Object parameter)
   {
      parameters.put(position, parameter);
   }

   public void setParameter(String name, Object parameter)
   {
      parameters.put(name, parameter);
   }

   public List<?> matches(Collection<?> entities)
   {
      List<Object[]> tuples = from.matches(entities);

      if (where != null) {
         filterTuplesAccordingToWhereClause(tuples);
      }

      return getResult(tuples);
   }

   private void filterTuplesAccordingToWhereClause(List<Object[]> tuples)
   {
      Map<String, Object> aliasToValue = new LinkedHashMap<String, Object>();
      from.getAliases(aliasToValue);

      for (Iterator<Object[]> itr = tuples.iterator(); itr.hasNext(); ) {
         Object[] tuple = itr.next();

         setValuesInAliasToValueMap(aliasToValue, tuple);

         Boolean satisfiesWhereCondition =
            (Boolean) where.evaluate(new QueryEval(parameters, aliasToValue));

         if (!satisfiesWhereCondition) {
            itr.remove();
         }
      }
   }

   private void setValuesInAliasToValueMap(Map<String, Object> aliasToValue, Object[] tuple)
   {
      int j = 0;

      for (Map.Entry<String, Object> aliasAndValue : aliasToValue.entrySet()) {
         aliasAndValue.setValue(tuple[j]);
         j++;
      }
   }

   private List<?> getResult(List<Object[]> tuples)
   {
      List<Object> result = new ArrayList<Object>();

      if (tuples.isEmpty()) {
         return result;
      }

      // TODO: handle distinct
      if (select != null) {
         extractSelectedPropertiesFromTuples(tuples, result);
      }
      else if (tuples.get(0).length > 1) {
         return tuples;
      }
      else {
         addSingleElementsToResult(tuples, result, 0);
      }

      return result;
   }

   private void extractSelectedPropertiesFromTuples(List<Object[]> tuples, List<Object> result)
   {
      // TODO: handle new
      List<PathAndAlias> selectedProperties = select.selectedProperties;
      int[] columns = columnIndexesForSelectedProperties(selectedProperties);

      if (columns.length == 1) {
         addSingleElementsToResult(tuples, result, columns[0]);
      }
      else {
         for (Object[] tuple : tuples) {
            Object[] selectedTuple = selectedTupleFromFullTuple(selectedProperties, columns, tuple);
            result.add(selectedTuple);
         }
      }
   }

   private Object[] selectedTupleFromFullTuple(List<PathAndAlias> selectedProperties,
      int[] columns, Object[] tuple)
   {
      Object[] selectedTuple = new Object[columns.length];
      int j = 0;

      for (PathAndAlias selectedProperty : selectedProperties) {
         Object value = tuple[columns[j]];
         selectedTuple[j] = selectedProperty.evaluate(value);
         j++;
      }

      return selectedTuple;
   }

   private int[] columnIndexesForSelectedProperties(List<PathAndAlias> selectedProperties)
   {
      int[] columns = new int[selectedProperties.size()];
      int j = 0;

      for (PathAndAlias selectedProperty : selectedProperties) {
         columns[j] = from.columnIndex(selectedProperty.pathElements[0]);
         j++;
      }

      return columns;
   }

   private void addSingleElementsToResult(List<Object[]> tuples, List<Object> result, int index)
   {
      for (Object[] tuple : tuples) {
         result.add(tuple[index]);
      }
   }
}