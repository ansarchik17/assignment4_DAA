package graph.main;

import graph.scc.TarjanSCC;
import graph.topo.TopoSort;
import graph.dagsp.DagShortestPath;
import graph.utils.DetailedMetrics;
import graph.utils.GraphGenerator;
import com.google.gson.*;

import java.nio.file.*;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws IOException {
        generateAllDatasets();

        String[] datasets = {
                "data/small1.json", "data/small2.json", "data/small3.json",
                "data/medium1.json", "data/medium2.json", "data/medium3.json",
                "data/large1.json", "data/large2.json", "data/large3.json"
        };

        String[] durationFiles = {
                "data/small1_durations.json", "data/small2_durations.json", "data/small3_durations.json",
                "data/medium1_durations.json", "data/medium2_durations.json", "data/medium3_durations.json",
                "data/large1_durations.json", "data/large2_durations.json", "data/large3_durations.json"
        };

        try (PrintWriter writer = new PrintWriter(new File("analysis_results.csv"))) {
            writer.println("Dataset,NumNodes,NumEdges,NumSCCs,MaxSCCSize,TopoOrderTimeMs,SCCTimeMs," +
                    "ShortestPathTimeMs,LongestPathTimeMs,CriticalPathLength,TotalOps,MemoryUsedKB");

            for (int i = 0; i < datasets.length; i++) {
                System.out.println("\n=== Processing " + datasets[i] + " ===");

                Map<Integer, List<Integer>> graph = loadGraph(datasets[i]);
                Map<Integer, Integer> durations = loadDurations(durationFiles[i]);

                DetailedMetrics metrics = new DetailedMetrics();

                // 1. SCC Analysis
                TarjanSCC scc = new TarjanSCC(metrics);
                List<List<Integer>> sccs = scc.findSCC(graph);
                Map<Integer, Integer> nodeToComp = scc.getNodeToComponentMap();
                Map<Integer, List<Integer>> condensation = scc.buildCondensationGraph(graph);

                long sccTime = metrics.getTimeNano();
                long sccOps = metrics.getOpsCount();
                long sccMemory = metrics.getMemoryUsed();

                // 2. Topological Sort
                metrics.reset();
                TopoSort topo = new TopoSort(metrics);
                List<Integer> topoOrder = topo.sort(condensation);
                long topoTime = metrics.getTimeNano();

                // 3. Path Analysis
                metrics.reset();
                DagShortestPath dsp = new DagShortestPath(metrics);
                DagShortestPath.PathResult shortest = dsp.shortestPath(condensation,
                        generateComponentDurations(condensation, sccs, durations), 0);
                long shortestTime = metrics.getTimeNano();

                metrics.reset();
                DagShortestPath.PathResult longest = dsp.longestPath(condensation,
                        generateComponentDurations(condensation, sccs, durations), 0);
                long longestTime = metrics.getTimeNano();

                int criticalPath = dsp.getCriticalPathLength(longest.distances);

                // Write results
                writer.printf("%s,%d,%d,%d,%d,%.3f,%.3f,%.3f,%.3f,%d,%d,%d\n",
                        Paths.get(datasets[i]).getFileName(),
                        graph.size(),
                        graph.values().stream().mapToInt(List::size).sum(),
                        sccs.size(),
                        sccs.stream().mapToInt(List::size).max().orElse(0),
                        topoTime / 1_000_000.0,
                        sccTime / 1_000_000.0,
                        shortestTime / 1_000_000.0,
                        longestTime / 1_000_000.0,
                        criticalPath,
                        sccOps,
                        sccMemory / 1024
                );

                printDetailedAnalysis(graph, sccs, condensation, topoOrder, shortest, longest, criticalPath);
            }
        }

        System.out.println("\nAnalysis complete! Results saved to analysis_results.csv");
    }

    private static void generateAllDatasets() throws IOException {
        new File("data").mkdirs();

        // Small datasets (6-10 nodes)
        generateDataset("small1", 8, 0.3, 0, "Simple DAG");
        generateDataset("small2", 10, 0.4, 1, "Single cycle");
        generateDataset("small3", 9, 0.35, 2, "Multiple small cycles");

        // Medium datasets (10-20 nodes)
        generateDataset("medium1", 15, 0.25, 0, "Medium DAG");
        generateDataset("medium2", 18, 0.3, 3, "Mixed with cycles");
        generateDataset("medium3", 20, 0.2, 2, "Sparse with SCCs");

        // Large datasets (20-50 nodes)
        generateDataset("large1", 35, 0.15, 0, "Large DAG");
        generateDataset("large2", 45, 0.1, 4, "Complex cyclic");
        generateDataset("large3", 50, 0.08, 5, "Multiple large SCCs");
    }

    private static void generateDataset(String name, int nodes, double density, int cycles, String description)
            throws IOException {
        Map<Integer, List<Integer>> graph;

        if (cycles == 0) {
            graph = GraphGenerator.generateDAG(nodes, density);
        } else if (cycles > 3) {
            graph = GraphGenerator.generateMixedGraph(nodes, density, cycles);
        } else {
            graph = GraphGenerator.generateCyclicGraph(nodes, density, cycles);
        }

        GraphGenerator.saveGraph(graph, "data/" + name + ".json");
        GraphGenerator.saveDurations(
                GraphGenerator.generateNodeDurations(graph, 1, 10),
                "data/" + name + "_durations.json"
        );

        System.out.println("Generated " + name + ": " + description + " (" + nodes + " nodes)");
    }

    private static Map<Integer, List<Integer>> loadGraph(String path) throws IOException {
        String json = Files.readString(Paths.get(path));
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        Map<Integer, List<Integer>> graph = new LinkedHashMap<>();

        for (String nodeStr : obj.keySet()) {
            int node = Integer.parseInt(nodeStr);
            List<Integer> neighbors = new ArrayList<>();
            JsonElement elem = obj.get(nodeStr);

            if (elem.isJsonArray()) {
                for (JsonElement e : elem.getAsJsonArray()) {
                    neighbors.add(e.getAsInt());
                }
            }
            graph.put(node, neighbors);
        }
        return graph;
    }

    private static Map<Integer, Integer> loadDurations(String path) throws IOException {
        String json = Files.readString(Paths.get(path));
        JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
        Map<Integer, Integer> durations = new HashMap<>();

        for (String nodeStr : obj.keySet()) {
            durations.put(Integer.parseInt(nodeStr), obj.get(nodeStr).getAsInt());
        }
        return durations;
    }

    private static Map<Integer, Integer> generateComponentDurations(
            Map<Integer, List<Integer>> condensation,
            List<List<Integer>> sccs,
            Map<Integer, Integer> nodeDurations) {
        Map<Integer, Integer> compDurations = new HashMap<>();

        for (int comp = 0; comp < sccs.size(); comp++) {
            int totalDuration = sccs.get(comp).stream()
                    .mapToInt(node -> nodeDurations.getOrDefault(node, 1))
                    .sum();
            compDurations.put(comp, totalDuration);
        }

        return compDurations;
    }

    private static void printDetailedAnalysis(Map<Integer, List<Integer>> graph,
                                              List<List<Integer>> sccs,
                                              Map<Integer, List<Integer>> condensation,
                                              List<Integer> topoOrder,
                                              DagShortestPath.PathResult shortest,
                                              DagShortestPath.PathResult longest,
                                              int criticalPath) {
        System.out.println("Graph Analysis:");
        System.out.println("- Nodes: " + graph.size());
        System.out.println("- Edges: " + graph.values().stream().mapToInt(List::size).sum());
        System.out.println("- SCCs: " + sccs.size());
        System.out.println("- Max SCC Size: " + sccs.stream().mapToInt(List::size).max().orElse(0));
        System.out.println("- Topological Order: " + topoOrder);
        System.out.println("- Critical Path Length: " + criticalPath);
        System.out.println("- Longest Path: " + longest.path);

        System.out.println("\nSCC Details:");
        for (int i = 0; i < sccs.size(); i++) {
            System.out.println("  Component " + i + ": " + sccs.get(i) + " (size: " + sccs.get(i).size() + ")");
        }
    }
}