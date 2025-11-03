package test;

import graph.scc.TarjanSCC;
import graph.topo.TopoSort;
import graph.dagsp.DagShortestPath;
import graph.utils.DetailedMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

class GraphAlgorithmsTest {
    private DetailedMetrics metrics;

    @BeforeEach
    void setUp() {
        metrics = new DetailedMetrics();
    }

    @Test
    void testDAGShortestPath() {
        Map<Integer, List<Integer>> graph = Map.of(
                0, List.of(1, 2),
                1, List.of(3),
                2, List.of(3),
                3, List.of()
        );

        Map<Integer, Integer> durations = Map.of(
                0, 1,
                1, 4,
                2, 2,
                3, 1
        );

        DagShortestPath dsp = new DagShortestPath(metrics);

        DagShortestPath.PathResult result = dsp.shortestPathToTarget(graph, durations, 0, 3);

        System.out.println("Shortest path to node 3: " + result.path);
        System.out.println("Distances: " + result.distances);

        assertEquals(List.of(0, 2, 3), result.path);

        assertEquals(1, result.distances.get(0));
        assertEquals(5, result.distances.get(1)); // 0->1: 1 + 4 = 5
        assertEquals(3, result.distances.get(2)); // 0->2: 1 + 2 = 3
        assertEquals(4, result.distances.get(3)); // 0->2->3: 1 + 2 + 1 = 4
    }


    @Test
    void testDAGLongestPath() {
        Map<Integer, List<Integer>> graph = Map.of(
                0, List.of(1, 2),
                1, List.of(3),
                2, List.of(3),
                3, List.of()
        );
        Map<Integer, Integer> durations = Map.of(
                0, 1,
                1, 4,
                2, 2,
                3, 1
        );

        DagShortestPath dsp = new DagShortestPath(metrics);
        DagShortestPath.PathResult result = dsp.longestPathToTarget(graph, durations, 0, 3);

        System.out.println("Longest path to node 3: " + result.path);

        assertEquals(List.of(0, 1, 3), result.path);

        assertEquals(1, result.distances.get(0));
        assertEquals(5, result.distances.get(1)); // 0->1: 1 + 4 = 5
        assertEquals(3, result.distances.get(2)); // 0->2: 1 + 2 = 3
        assertEquals(6, result.distances.get(3)); // 0->1->3: 1 + 4 + 1 = 6
    }

    @Test
    void testDAGShortestPathSimpleLinear() {
        // Simple linear graph: 0 -> 1 -> 2
        Map<Integer, List<Integer>> graph = Map.of(
                0, List.of(1),
                1, List.of(2),
                2, List.of()
        );
        Map<Integer, Integer> durations = Map.of(
                0, 1,
                1, 2,
                2, 3
        );

        DagShortestPath dsp = new DagShortestPath(metrics);
        DagShortestPath.PathResult result = dsp.shortestPathToTarget(graph, durations, 0, 2);

        assertEquals(List.of(0, 1, 2), result.path);

        assertEquals(1, result.distances.get(0));
        assertEquals(3, result.distances.get(1)); // 0->1: 1 + 2 = 3
        assertEquals(6, result.distances.get(2)); // 0->1->2: 1 + 2 + 3 = 6
    }

    @Test
    void testSCCSimpleDAG() {
        Map<Integer, List<Integer>> graph = Map.of(
                0, List.of(1),
                1, List.of(2),
                2, List.of(3),
                3, List.of()
        );

        TarjanSCC scc = new TarjanSCC(metrics);
        List<List<Integer>> components = scc.findSCC(graph);

        assertEquals(4, components.size());
        assertTrue(components.stream().allMatch(c -> c.size() == 1));
    }

    @Test
    void testTopologicalSort() {
        Map<Integer, List<Integer>> graph = Map.of(
                0, List.of(1, 2),
                1, List.of(3),
                2, List.of(3),
                3, List.of()
        );

        TopoSort topo = new TopoSort(metrics);
        List<Integer> order = topo.sort(graph);

        assertEquals(4, order.size());
        assertTrue(order.indexOf(0) < order.indexOf(1));
        assertTrue(order.indexOf(0) < order.indexOf(2));
        assertTrue(order.indexOf(1) < order.indexOf(3));
        assertTrue(order.indexOf(2) < order.indexOf(3));
    }

    @Test
    void testPathReconstruction() {
        Map<Integer, Integer> predecessors = new HashMap<>();
        predecessors.put(1, 0);
        predecessors.put(2, 0);
        predecessors.put(3, 2);
        predecessors.put(4, 3);

        Map<Integer, List<Integer>> graph = Map.of(
                0, List.of(1, 2),
                1, List.of(3),
                2, List.of(3),
                3, List.of(4),
                4, List.of()
        );
        Map<Integer, Integer> durations = Map.of(
                0, 1, 1, 1, 2, 1, 3, 1, 4, 1
        );

        DagShortestPath dsp = new DagShortestPath(metrics);
        DagShortestPath.PathResult result = dsp.shortestPathToTarget(graph, durations, 0, 4);

        assertFalse(result.path.isEmpty());
        assertEquals(0, result.path.get(0).intValue());
        assertEquals(4, result.path.get(result.path.size() - 1).intValue());
        assertTrue(result.path.size() >= 3); // At least 3 nodes: 0, intermediate, 4
    }
}