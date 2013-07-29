/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.reporting.dataCoverage;

import mockit.coverage.dataItems.*;
import mockit.coverage.reporting.parsing.*;

public final class DataCoverageOutput
{
   private final StringBuilder openingTag;
   private final PerFileDataCoverage coverageInfo;
   private int nextField;
   private String classAndFieldNames;
   private String className;
   private String fieldName;

   public DataCoverageOutput(PerFileDataCoverage coverageInfo)
   {
      openingTag = new StringBuilder(50);
      this.coverageInfo = coverageInfo;
      moveToNextField();
   }

   private void moveToNextField()
   {
      if (nextField >= coverageInfo.allFields.size()) {
         classAndFieldNames = null;
         className = null;
         fieldName = null;
         return;
      }

      classAndFieldNames = coverageInfo.allFields.get(nextField);
      nextField++;

      int p = classAndFieldNames.indexOf('.');
      className = classAndFieldNames.substring(0, p);
      fieldName = classAndFieldNames.substring(p + 1);
   }

   public void writeCoverageInfoIfLineStartsANewFieldDeclaration(FileParser fileParser)
   {
      if (classAndFieldNames != null && className.equals(fileParser.getCurrentlyPendingClass())) {
         LineElement initialLineElement = fileParser.lineParser.getInitialElement();
         LineElement elementWithFieldName = initialLineElement.findWord(fieldName);

         if (elementWithFieldName != null) {
            buildOpeningTagForFieldWrapper();
            elementWithFieldName.wrapText(openingTag.toString(), "</span>");
            moveToNextField();
         }
      }
   }

   private void buildOpeningTagForFieldWrapper()
   {
      openingTag.setLength(0);
      openingTag.append("<span class='");

      StaticFieldData staticData = coverageInfo.getStaticFieldData(classAndFieldNames);
      boolean staticField = staticData != null;
      openingTag.append(staticField ? "static" : "instance");

      openingTag.append(coverageInfo.isCovered(classAndFieldNames) ? " covered" : " uncovered");

      InstanceFieldData instanceData = coverageInfo.getInstanceFieldData(classAndFieldNames);

      if (staticField || instanceData != null) {
         openingTag.append("' title='");
         appendAccessCounts(staticField ? staticData : instanceData);
      }

      openingTag.append("'>");
   }

   private void appendAccessCounts(FieldData fieldData)
   {
      openingTag.append("Reads: ").append(fieldData.getReadCount());
      openingTag.append(" Writes: ").append(fieldData.getWriteCount());
   }
}
