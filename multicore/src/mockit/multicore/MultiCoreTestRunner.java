/*
 * Copyright (c) 2006-2013 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.multicore;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public final class MultiCoreTestRunner
{
   public static boolean inactive;
   private static MultiCoreTestRunner instance;

   public static MultiCoreTestRunner instance()
   {
      if (instance == null && !inactive) {
         instance = new MultiCoreTestRunner();
      }

      return instance;
   }

   private final ProcessBuilder processBuilder;
   private final Queue<TestClassRunnerTask> tasks;
   private final TestExecutionTask[] perCoreTestExecutions;
   private volatile int activeSubProcesses;
   private String pendingOutput;

   private final class TestExecutionTask extends Thread
   {
      private final Process subProcess;
      private final StreamIO streamIO;

      TestExecutionTask()
      {
         try { subProcess = processBuilder.start(); } catch (IOException e) { throw new RuntimeException(e); }
         streamIO = new StreamIO(subProcess.getInputStream(), subProcess.getOutputStream());
      }

      @Override
      public void run()
      {
         TestClassRunnerTask newTask;

         while ((newTask = tasks.poll()) != null) {
            newTask.runTestClass(streamIO, MultiCoreTestRunner.this);
         }

         subProcess.destroy();
         notifyOfSubProcessCompletion();
      }

      private void notifyOfSubProcessCompletion()
      {
         activeSubProcesses--;
         registerPendingOutputFromSubProcess(null);
      }
   }

   private MultiCoreTestRunner()
   {
      processBuilder = new ProcessBuilderCreation().processBuilder;
      tasks = new ConcurrentLinkedQueue<TestClassRunnerTask>();

      int numCores = Runtime.getRuntime().availableProcessors();
      perCoreTestExecutions = new TestExecutionTask[numCores];

      createTestClassExecutionForEachCPUCore();
   }

   private void createTestClassExecutionForEachCPUCore()
   {
      for (int i = 0; i < perCoreTestExecutions.length; i++) {
         perCoreTestExecutions[i] = new TestExecutionTask();
      }
   }

   public void addTask(TestClassRunnerTask task) { tasks.add(task); }

   public void runAllTasksToCompletion()
   {
      activeSubProcesses = perCoreTestExecutions.length;

      for (Thread thread : perCoreTestExecutions) {
         thread.start();
      }

      while (activeSubProcesses > 0) {
         synchronized (this) {
            try { wait(0); } catch (InterruptedException ignore) {}

            if (pendingOutput != null) {
               System.out.print(pendingOutput);
               pendingOutput = null;
            }
         }
      }
   }

   void registerPendingOutputFromSubProcess(String output)
   {
      synchronized (this) {
         pendingOutput = output;
         notifyAll();
      }
   }
}
