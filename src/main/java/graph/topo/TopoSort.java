package graph.topo;

import graph.utils.DetailedMetrics;
import java.util.*;

public class TopoSort {
    private final DetailedMetrics metrics;

    public TopoSort(DetailedMetrics metrics) {
        this.metrics = metrics;
    }

    public List<Integer> sort(Map<Integer, List<Integer>> graph) {
        metrics.start();

        Map<Integer, Integer> inDegree = new HashMap<>();
        for (int node : graph.keySet()) {
            inDegree.putIfAbsent(node, 0);
            for (int neighbor : graph.get(node)) {
                inDegree.put(neighbor, inDegree.getOrDefault(neighbor, 0) + 1);
                metrics.incrementOps(); // Edge processing
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int node : graph.keySet()) {
            metrics.incrementOps(); // Node processing
            if (inDegree.getOrDefault(node, 0) == 0) {
                queue.offer(node);
            }
        }

        List<Integer> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            int node = queue.poll();
            topoOrder.add(node);
            metrics.incrementOps(); // Node processing

            for (int neighbor : graph.getOrDefault(node, Collections.emptyList())) {
                inDegree.put(neighbor, inDegree.get(neighbor) - 1);
                if (inDegree.get(neighbor) == 0) {
                    queue.offer(neighbor);
                }
                metrics.incrementOps(); // Edge processing
            }
        }

        // Check for cycles
        if (topoOrder.size() != graph.size()) {
            throw new IllegalArgumentException("Graph has cycles - topological sort not possible");
        }

        metrics.stop();
        return topoOrder;
    }

    public List<Integer> sortDFS(Map<Integer, List<Integer>> graph) {
        metrics.start();
        Set<Integer> visited = new HashSet<>();
        Set<Integer> recursionStack = new HashSet<>();
        Stack<Integer> stack = new Stack<>();

        for (int node : graph.keySet()) {
            metrics.incrementOps(); // Node processing
            if (!visited.contains(node)) {
                if (dfs(node, graph, visited, recursionStack, stack)) {
                    throw new IllegalArgumentException("Graph contains cycles");
                }
            }
        }

        List<Integer> result = new ArrayList<>();
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }

        metrics.stop();
        return result;
    }

    private boolean dfs(int node, Map<Integer, List<Integer>> graph,
                        Set<Integer> visited, Set<Integer> recursionStack,
                        Stack<Integer> stack) {
        metrics.incrementOps(); // DFS call
        if (recursionStack.contains(node)) {
            return true; // Cycle detected
        }
        if (visited.contains(node)) {
            return false;
        }

        visited.add(node);
        recursionStack.add(node);

        for (int neighbor : graph.getOrDefault(node, Collections.emptyList())) {
            metrics.incrementOps();
            if (dfs(neighbor, graph, visited, recursionStack, stack)) {
                return true;
            }
        }

        recursionStack.remove(node);
        stack.push(node);
        return false;
    }
}