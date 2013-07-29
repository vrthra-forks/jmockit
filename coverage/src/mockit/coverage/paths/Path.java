/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.paths;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.*;

public final class Path implements Serializable
{
   private static final long serialVersionUID = 8895491272907955543L;

   final List<Node> nodes = new ArrayList<Node>(4);
   private final AtomicInteger executionCount = new AtomicInteger();
   private final boolean shadowed;
   private Path shadowPath;

   Path(Node.Entry entryNode)
   {
      shadowed = false;
      addNode(entryNode);
   }

   Path(Path sharedSubPath, boolean shadowed)
   {
      this.shadowed = shadowed;
      sharedSubPath.shadowPath = shadowed ? this : null;
      nodes.addAll(sharedSubPath.nodes);
   }

   void addNode(Node node) { nodes.add(node); }

   boolean countExecutionIfAllNodesWereReached(List<Node> nodesReached)
   {
      boolean allNodesReached = nodes.equals(nodesReached);

      if (allNodesReached) {
         executionCount.getAndIncrement();
      }

      return allNodesReached;
   }

   public boolean isShadowed() { return shadowed; }
   public List<Node> getNodes() { return nodes; }

   public int getExecutionCount()
   {
      int count = executionCount.get();

      if (shadowPath != null) {
         count += shadowPath.executionCount.get();
      }

      return count;
   }

   void addCountFromPreviousTestRun(Path previousPath)
   {
      int previousExecutionCount = previousPath.executionCount.get();
      executionCount.set(previousExecutionCount);
   }

   void reset()
   {
      executionCount.set(0);
   }
}
