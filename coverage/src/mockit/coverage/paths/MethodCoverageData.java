/*
 * Copyright (c) 2006-2012 Rogério Liesenfeld
 * This file is subject to the terms of the MIT license (see LICENSE.txt).
 */
package mockit.coverage.paths;

import java.io.*;
import java.util.*;

import mockit.coverage.paths.Node.*;

public final class MethodCoverageData implements Serializable
{
   private static final long serialVersionUID = -5073393714435522417L;

   public final String methodName;
   private List<Node> nodes;
   private int firstLine;
   private int lastLine;

   // Helper fields used during node building and path execution:
   private transient ThreadLocal<List<Node>> nodesReached;
   private transient ThreadLocal<Integer> previousNodeIndex;

   public List<Path> paths;
   private List<Path> nonShadowedPaths;

   public MethodCoverageData(String methodName) { this.methodName = methodName; }

   public void buildPaths(int lastLine, NodeBuilder nodeBuilder)
   {
      firstLine = nodeBuilder.firstLine;
      this.lastLine = lastLine;

      nodes = nodeBuilder.nodes;
      paths = new PathBuilder().buildPaths(nodes);
      buildListOfNonShadowedPaths();

      nodesReached = new ThreadLocal<List<Node>>();
      previousNodeIndex = new ThreadLocal<Integer>();
   }

   private void buildListOfNonShadowedPaths()
   {
      nonShadowedPaths = new ArrayList<Path>(paths.size());

      for (Path path : paths) {
         if (!path.isShadowed()) {
            nonShadowedPaths.add(path);
         }
      }
   }

   public int getFirstLineInBody() { return firstLine; }
   public int getLastLineInBody() { return lastLine; }

   public void markNodeAsReached(int nodeIndex)
   {
      if (nodeIndex == 0) {
         clearNodes();
      }

      Node node = nodes.get(nodeIndex);
      List<Node> currentNodesReached = nodesReached.get();

      if (!node.wasReached() && (nodeIndex == 0 || nodeIndex > previousNodeIndex.get())) {
         node.setReached(Boolean.TRUE);
         currentNodesReached.add(node);
         previousNodeIndex.set(nodeIndex);
      }

      if (node instanceof Exit) {
         Exit exitNode = (Exit) node;

         for (Path path : exitNode.paths) {
            if (path.countExecutionIfAllNodesWereReached(currentNodesReached)) {
               return;
            }
         }
      }
   }

   private void clearNodes()
   {
      for (Node node : nodes) {
         node.setReached(null);
      }

      nodesReached.set(new ArrayList<Node>());
      previousNodeIndex.set(0);
   }

   public List<Path> getPaths() { return nonShadowedPaths; }

   public int getExecutionCount()
   {
      int totalCount = 0;

      for (Path path : nonShadowedPaths) {
         totalCount += path.getExecutionCount();
      }

      return totalCount;
   }

   public int getTotalPaths() { return nonShadowedPaths.size(); }

   public int getCoveredPaths()
   {
      int coveredCount = 0;

      for (Path path : nonShadowedPaths) {
         if (path.getExecutionCount() > 0) {
            coveredCount++;
         }
      }

      return coveredCount;
   }

   public void addCountsFromPreviousTestRun(MethodCoverageData previousData)
   {
      for (int i = 0; i < paths.size(); i++) {
         Path path = paths.get(i);
         Path previousPath = previousData.paths.get(i);
         path.addCountFromPreviousTestRun(previousPath);
      }
   }

   public void reset()
   {
      clearNodes();

      for (Path path : paths) {
         path.reset();
      }
   }
}
