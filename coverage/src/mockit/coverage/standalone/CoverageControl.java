/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.standalone;

import java.io.*;
import java.lang.management.*;
import java.lang.reflect.*;
import java.util.prefs.*;
import javax.management.*;

import mockit.coverage.*;

public final class CoverageControl extends StandardMBean implements CoverageControlMBean, PersistentMBean
{
   static void create()
   {
      MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();

      try {
         CoverageControl mxBean = new CoverageControl();
         mbeanServer.registerMBean(mxBean, new ObjectName("JMockit Coverage:type=CoverageControl"));
      }
      catch (JMException e) {
         throw new RuntimeException(e);
      }
   }

   public CoverageControl() throws NotCompliantMBeanException, MBeanException
   {
      super(CoverageControlMBean.class);
      load();
   }

   @Override
   protected String getDescription(MBeanInfo info)
   {
      return CoverageControlMBean.class.getAnnotation(Description.class).value();
   }

   @Override
   protected String getParameterName(MBeanOperationInfo op, MBeanParameterInfo param, int sequence)
   {
      return "resetState";
   }

   @Override
   protected String getDescription(MBeanAttributeInfo info) { return getDescription("get" + info.getName()); }

   private String getDescription(String methodName)
   {
      return getMethod(methodName).getAnnotation(Description.class).value();
   }

   private Method getMethod(String methodName)
   {
      for (Method method : CoverageControlMBean.class.getDeclaredMethods()) {
         if (method.getName().equals(methodName)) {
            return method;
         }
      }

      return null;
   }

   @Override
   protected String getDescription(MBeanOperationInfo info) { return getDescription(info.getName()); }

   @Override
   protected String getDescription(MBeanOperationInfo op, MBeanParameterInfo param, int sequence)
   {
      Method method = getMethod(op.getName());
      Description desc = (Description) method.getParameterAnnotations()[sequence][0];
      return desc.value();
   }

   @Override
   protected int getImpact(MBeanOperationInfo info) { return MBeanOperationInfo.ACTION; }

   public String getOutput() { return getProperty("output", "html").replace("-nocp", ""); }
   public void setOutput(String output)
   { modifyConfigurationProperty("output", "html".equals(output) ? "html-nocp" : output); }

   public String getWorkingDir() { return new File(".").getAbsoluteFile().getParent(); }

   public String getOutputDir() { return getProperty("outputDir"); }
   public void setOutputDir(String outputDir) { modifyConfigurationProperty("outputDir", outputDir); }

   public String getSrcDirs() { return getProperty("srcDirs"); }
   public void setSrcDirs(String srcDirs) { modifyConfigurationProperty("srcDirs", srcDirs); }

   public String getClasses() { return getProperty("classes"); }
   public void setClasses(String classes) { modifyConfigurationProperty("classes", classes); }

   public String getExcludes() { return getProperty("excludes"); }
   public void setExcludes(String excludes) { modifyConfigurationProperty("excludes", excludes); }

   public String getMetrics() { return getProperty("metrics", "all"); }
   public void setMetrics(String metrics) { modifyConfigurationProperty("metrics", metrics); }

   private String getProperty(String property) { return System.getProperty(propertyName(property), ""); }
   private String getProperty(String property, String defaultValue)
   { return System.getProperty(propertyName(property), defaultValue); }

   private String propertyName(String name)
   {
      return "jmockit-coverage-" + Character.toLowerCase(name.charAt(0)) + name.substring(1);
   }

   private void modifyConfigurationProperty(String name, String value)
   {
      setConfigurationProperty(name, value);
      CodeCoverage.resetConfiguration();
      store();
   }

   private void setConfigurationProperty(String name, String value) { System.setProperty(propertyName(name), value); }

   public void generateOutput(boolean resetState)
   {
      CodeCoverage.generateOutput(resetState);
   }

   public void load() throws MBeanException
   {
      Preferences preferences = Preferences.userNodeForPackage(CoverageControl.class);

      try {
         for (String property : preferences.keys()) {
            String commandLineValue = getProperty(property);

            if (commandLineValue.length() == 0) {
               String value = preferences.get(property, "");
               setConfigurationProperty(property, value);
            }
         }
      }
      catch (BackingStoreException e) {
         throw new MBeanException(e);
      }
   }

   public void store()
   {
      Preferences preferences = Preferences.userNodeForPackage(CoverageControl.class);

      for (MBeanAttributeInfo info : getMBeanInfo().getAttributes()) {
         String property = info.getName();
         String value = getProperty(property);
         preferences.put(property, value);
      }

      try {
          preferences.flush();
      }
      catch (BackingStoreException e) {
         throw new RuntimeException(e);
      }
   }
}
