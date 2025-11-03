package graph.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class GraphGenerator {
    private static final Random rand = new Random(42);
    public static Map<Integer, List<Integer>> generateDAG(int n, double density) {
        Map<Integer, List<Integer>> graph = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            graph.put(i, new ArrayList<>());
        }

        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                if (rand.nextDouble() < density) {
                    graph.get(i).add(j);
                }
            }
        }
        return graph;
    }

    public static Map<Integer, List<Integer>> generateCyclicGraph(int n, double density, int cycles) {
        Map<Integer, List<Integer>> graph = generateDAG(n, density);

        // Add cycles
        for (int i = 0; i < cycles; i++) {
            int cycleLength = rand.nextInt(3) + 3;
            List<Integer> nodes = new ArrayList<>();
            for (int j = 0; j < cycleLength; j++) {
                nodes.add(rand.nextInt(n));
            }

            // Create cycle
            for (int j = 0; j < cycleLength; j++) {
                int from = nodes.get(j);
                int to = nodes.get((j + 1) % cycleLength);
                if (!graph.get(from).contains(to)) {
                    graph.get(from).add(to);
                }
            }
        }

        return graph;
    }

    public static Map<Integer, List<Integer>> generateMixedGraph(int n, double density, int sccCount) {
        Map<Integer, List<Integer>> graph = new LinkedHashMap<>();
        for (int i = 0; i < n; i++) {
            graph.put(i, new ArrayList<>());
        }

        // Create SCCs
        int nodesPerSCC = n / sccCount;
        for (int scc = 0; scc < sccCount; scc++) {
            int start = scc * nodesPerSCC;
            int end = (scc == sccCount - 1) ? n : start + nodesPerSCC;

            // Create edges within SCC
            for (int i = start; i < end; i++) {
                for (int j = start; j < end; j++) {
                    if (i != j && rand.nextDouble() < density * 2) {
                        graph.get(i).add(j);
                    }
                }
            }
        }

        // Add edges between SCCs (creating DAG structure between components)
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                int compI = i / nodesPerSCC;
                int compJ = j / nodesPerSCC;
                if (compI < compJ && rand.nextDouble() < density) {
                    graph.get(i).add(j);
                }
            }
        }

        return graph;
    }

    public static Map<Integer, Integer> generateNodeDurations(Map<Integer, List<Integer>> graph, int min, int max) {
        Map<Integer, Integer> durations = new HashMap<>();
        for (int node : graph.keySet()) {
            durations.put(node, rand.nextInt(max - min + 1) + min);
        }
        return durations;
    }

    public static void saveGraph(Map<Integer, List<Integer>> graph, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("{\n");
            int count = 0;
            for (var entry : graph.entrySet()) {
                writer.write("  \"" + entry.getKey() + "\": " +
                        entry.getValue().toString().replace(" ", ""));
                if (count++ < graph.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("}");
        }
    }

    public static void saveDurations(Map<Integer, Integer> durations, String filename) throws IOException {
        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("{\n");
            int count = 0;
            for (var entry : durations.entrySet()) {
                writer.write("  \"" + entry.getKey() + "\": " + entry.getValue());
                if (count++ < durations.size() - 1) writer.write(",");
                writer.write("\n");
            }
            writer.write("}");
        }
    }
}