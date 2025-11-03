package graph.dagsp;

import graph.utils.DetailedMetrics;
import java.util.*;

public class DagShortestPath {
    private final DetailedMetrics metrics;

    public DagShortestPath(DetailedMetrics metrics) {
        this.metrics = metrics;
    }

    public static class PathResult {
        public final Map<Integer, Integer> distances;
        public final Map<Integer, Integer> predecessors;
        public final List<Integer> path;
        public final int target;

        public PathResult(Map<Integer, Integer> distances,
                          Map<Integer, Integer> predecessors,
                          List<Integer> path, int target) {
            this.distances = distances;
            this.predecessors = predecessors;
            this.path = path;
            this.target = target;
        }
    }

    public PathResult shortestPath(Map<Integer, List<Integer>> dag,
                                   Map<Integer, Integer> nodeDurations,
                                   int source) {
        return shortestPathToTarget(dag, nodeDurations, source, -1);
    }

    public PathResult shortestPathToTarget(Map<Integer, List<Integer>> dag,
                                           Map<Integer, Integer> nodeDurations,
                                           int source, int specificTarget) {
        metrics.start();

        // Initialize distances and predecessors
        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> pred = new HashMap<>();

        for (int node : dag.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
            pred.put(node, -1);
            metrics.incrementOps();
        }
        // Source distance is just its own duration
        dist.put(source, nodeDurations.getOrDefault(source, 0));
        pred.put(source, -1);

        // Get topological order
        graph.topo.TopoSort topo = new graph.topo.TopoSort(metrics);
        List<Integer> order = topo.sort(dag);

        // Process nodes in topological order
        for (int u : order) {
            if (dist.get(u) != Integer.MAX_VALUE) {
                // For each neighbor, update distance if we found a shorter path
                for (int v : dag.getOrDefault(u, Collections.emptyList())) {
                    metrics.incrementOps();

                    // The distance to v via u
                    int newDist = dist.get(u) + nodeDurations.getOrDefault(v, 1);

                    if (newDist < dist.get(v)) {
                        dist.put(v, newDist);
                        pred.put(v, u);
                    }
                }
            }
        }

        // Determine target node
        int target;
        if (specificTarget != -1 && dist.get(specificTarget) != Integer.MAX_VALUE) {
            target = specificTarget;
        } else {
            target = findFurthestReachableNode(dist, source);
        }

        List<Integer> path = reconstructPath(pred, source, target);

        metrics.stop();
        return new PathResult(dist, pred, path, target);
    }

    public PathResult longestPath(Map<Integer, List<Integer>> dag,
                                  Map<Integer, Integer> nodeDurations,
                                  int source) {
        return longestPathToTarget(dag, nodeDurations, source, -1);
    }

    public PathResult longestPathToTarget(Map<Integer, List<Integer>> dag,
                                          Map<Integer, Integer> nodeDurations,
                                          int source, int specificTarget) {
        metrics.start();

        Map<Integer, Integer> dist = new HashMap<>();
        Map<Integer, Integer> pred = new HashMap<>();

        for (int node : dag.keySet()) {
            dist.put(node, Integer.MIN_VALUE);
            pred.put(node, -1);
            metrics.incrementOps();
        }
        dist.put(source, nodeDurations.getOrDefault(source, 0));
        pred.put(source, -1);

        graph.topo.TopoSort topo = new graph.topo.TopoSort(metrics);
        List<Integer> order = topo.sort(dag);

        for (int u : order) {
            if (dist.get(u) != Integer.MIN_VALUE) {
                for (int v : dag.getOrDefault(u, Collections.emptyList())) {
                    metrics.incrementOps();
                    int newDist = dist.get(u) + nodeDurations.getOrDefault(v, 1);

                    if (newDist > dist.get(v)) {
                        dist.put(v, newDist);
                        pred.put(v, u);
                    }
                }
            }
        }

        int target;
        if (specificTarget != -1 && dist.get(specificTarget) != Integer.MIN_VALUE) {
            target = specificTarget;
        } else {
            target = findFurthestReachableNode(dist, source);
        }

        List<Integer> path = reconstructPath(pred, source, target);

        metrics.stop();
        return new PathResult(dist, pred, path, target);
    }

    private List<Integer> reconstructPath(Map<Integer, Integer> predecessors,
                                          int source, int target) {
        if (target == -1) {
            return new ArrayList<>();
        }

        List<Integer> path = new ArrayList<>();
        int current = target;

        // Backtrack from target to source
        while (current != -1) {
            path.add(0, current);
            current = predecessors.get(current);
        }

        // Verify the path starts with source
        if (!path.isEmpty() && path.get(0) != source) {
            // This shouldn't happen if the graph is connected, but handle it
            path.add(0, source);
        }

        return path;
    }

    private int findFurthestReachableNode(Map<Integer, Integer> distances, int source) {
        int furthestNode = source;
        int maxDistance = distances.get(source);

        for (Map.Entry<Integer, Integer> entry : distances.entrySet()) {
            int node = entry.getKey();
            int distance = entry.getValue();

            // Skip unreachable nodes and the source itself
            if (distance == Integer.MAX_VALUE || distance == Integer.MIN_VALUE || node == source) {
                continue;
            }

            // For shortest path, we want the node with maximum distance (critical path)
            // For longest path, same logic applies
            if (distance > maxDistance) {
                maxDistance = distance;
                furthestNode = node;
            }
        }

        return furthestNode;
    }

    public int getCriticalPathLength(Map<Integer, Integer> distances) {
        return distances.values().stream()
                .filter(d -> d != Integer.MIN_VALUE && d != Integer.MAX_VALUE)
                .max(Integer::compareTo)
                .orElse(0);
    }
}