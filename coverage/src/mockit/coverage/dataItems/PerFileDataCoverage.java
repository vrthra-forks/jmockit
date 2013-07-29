/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.dataItems;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import mockit.coverage.*;
import mockit.coverage.data.*;

public final class PerFileDataCoverage implements PerFileCoverage
{
   private static final long serialVersionUID = -4561686103982673490L;

   public final List<String> allFields = new ArrayList<String>(2);
   public final Map<String, StaticFieldData> staticFieldsData = new LinkedHashMap<String, StaticFieldData>();
   public final Map<String, InstanceFieldData> instanceFieldsData = new LinkedHashMap<String, InstanceFieldData>();

   private transient int coveredDataItems = -1;

   private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
   {
      coveredDataItems = -1;
      in.defaultReadObject();
   }

   public void addField(String className, String fieldName, boolean isStatic)
   {
      String classAndField = className + '.' + fieldName;
      allFields.add(classAndField);

      if (isStatic) {
         staticFieldsData.put(classAndField, new StaticFieldData());
      }
      else {
         instanceFieldsData.put(classAndField, new InstanceFieldData());
      }
   }

   public boolean isFieldWithCoverageData(String classAndFieldNames)
   {
      return
         instanceFieldsData.containsKey(classAndFieldNames) ||
         staticFieldsData.containsKey(classAndFieldNames);
   }

   public void registerAssignmentToStaticField(String classAndFieldNames)
   {
      StaticFieldData staticData = getStaticFieldData(classAndFieldNames);
      staticData.registerAssignment();
   }

   public StaticFieldData getStaticFieldData(String classAndFieldNames)
   {
      return staticFieldsData.get(classAndFieldNames);
   }

   public void registerReadOfStaticField(String classAndFieldNames)
   {
      StaticFieldData staticData = getStaticFieldData(classAndFieldNames);
      staticData.registerRead();
   }

   public void registerAssignmentToInstanceField(Object instance, String classAndFieldNames)
   {
      InstanceFieldData instanceData = getInstanceFieldData(classAndFieldNames);
      instanceData.registerAssignment(instance);
   }

   public InstanceFieldData getInstanceFieldData(String classAndFieldNames)
   {
      return instanceFieldsData.get(classAndFieldNames);
   }

   public void registerReadOfInstanceField(Object instance, String classAndFieldNames)
   {
      InstanceFieldData instanceData = getInstanceFieldData(classAndFieldNames);
      instanceData.registerRead(instance);
   }

   public boolean hasFields() { return !allFields.isEmpty(); }

   public boolean isCovered(String classAndFieldNames)
   {
      InstanceFieldData instanceData = getInstanceFieldData(classAndFieldNames);

      if (instanceData != null && instanceData.isCovered()) {
         return true;
      }

      StaticFieldData staticData = getStaticFieldData(classAndFieldNames);

      return staticData != null && staticData.isCovered();
   }

   public int getTotalItems()
   {
      return staticFieldsData.size() + instanceFieldsData.size();
   }

   public int getCoveredItems()
   {
      if (coveredDataItems >= 0) {
         return coveredDataItems;
      }

      coveredDataItems = 0;

      for (StaticFieldData staticData : staticFieldsData.values()) {
         if (staticData.isCovered()) {
            coveredDataItems++;
         }
      }

      for (InstanceFieldData instanceData : instanceFieldsData.values()) {
         if (instanceData.isCovered()) {
            coveredDataItems++;
         }
      }

      return coveredDataItems;
   }

   public int getCoveragePercentage()
   {
      int totalFields = getTotalItems();

      if (totalFields == 0) {
         return -1;
      }

      return CoveragePercentage.calculate(getCoveredItems(), totalFields);
   }

   public void mergeInformation(PerFileDataCoverage previousInfo)
   {
      addInfoFromPreviousTestRun(staticFieldsData, previousInfo.staticFieldsData);
      addFieldsFromPreviousTestRunIfAbsent(staticFieldsData, previousInfo.staticFieldsData);

      addInfoFromPreviousTestRun(instanceFieldsData, previousInfo.instanceFieldsData);
      addFieldsFromPreviousTestRunIfAbsent(instanceFieldsData, previousInfo.instanceFieldsData);
   }

   private <FI extends FieldData> void addInfoFromPreviousTestRun(
      Map<String, FI> currentInfo, Map<String, FI> previousInfo)
   {
      for (Entry<String, FI> nameAndInfo : currentInfo.entrySet()) {
         String fieldName = nameAndInfo.getKey();
         FieldData previousFieldInfo = previousInfo.get(fieldName);

         if (previousFieldInfo != null) {
            FieldData fieldInfo = nameAndInfo.getValue();
            fieldInfo.addCountsFromPreviousTestRun(previousFieldInfo);
         }
      }
   }

   private <FI extends FieldData> void addFieldsFromPreviousTestRunIfAbsent(
      Map<String, FI> currentInfo, Map<String, FI> previousInfo)
   {
      for (Entry<String, FI> nameAndInfo : previousInfo.entrySet()) {
         String fieldName = nameAndInfo.getKey();

         if (!currentInfo.containsKey(fieldName)) {
            currentInfo.put(fieldName, previousInfo.get(fieldName));
         }
      }
   }
}
