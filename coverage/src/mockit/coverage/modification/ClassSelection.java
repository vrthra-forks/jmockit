/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.modification;

import java.security.*;
import java.util.regex.*;

import mockit.coverage.standalone.*;

final class ClassSelection
{
   private final Matcher classesToInclude;
   private final Matcher classesToExclude;
   private final Matcher testCode;

   ClassSelection()
   {
      classesToInclude = getClassNameRegex("classes");
      classesToExclude = getClassNameRegex("excludes");
      testCode = Startup.isTestRun() ? Pattern.compile(".+Test(\\$.+)?").matcher("") : null;
   }

   private Matcher getClassNameRegex(String propertySuffix)
   {
      String regex = System.getProperty("jmockit-coverage-" + propertySuffix, "");
      return regex.length() == 0 ? null : Pattern.compile(regex).matcher("");
   }

   boolean isSelected(String className, ProtectionDomain protectionDomain)
   {
      CodeSource codeSource = protectionDomain.getCodeSource();

      if (
         codeSource == null || className.charAt(0) == '[' || className.startsWith("mockit.") ||
         className.startsWith("org.junit.") || className.startsWith("junit.") || className.startsWith("org.testng.")
      ) {
         return false;
      }

      if (classesToExclude != null && classesToExclude.reset(className).matches()) {
         return false;
      }
      else if (classesToInclude != null && classesToInclude.reset(className).matches()) {
         return true;
      }
      else if (testCode != null && testCode.reset(className).matches()) {
         return false;
      }

      String location = codeSource.getLocation().getPath();

      return
         !location.endsWith(".jar") && !location.endsWith("/.cp/") &&
         (testCode == null || !location.endsWith("/test-classes/") && !location.endsWith("/jmockit/main/classes/"));
   }
}
