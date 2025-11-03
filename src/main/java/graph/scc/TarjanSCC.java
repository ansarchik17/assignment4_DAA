package graph.scc;

import graph.utils.DetailedMetrics;
import java.util.*;

public class TarjanSCC {
    private final DetailedMetrics metrics;
    private Map<Integer, List<Integer>> graph;
    private int index;
    private Map<Integer, Integer> indexes, lowlink;
    private Stack<Integer> stack;
    private Set<Integer> onStack;
    private List<List<Integer>> sccList;
    private Map<Integer, Integer> nodeToComponent;

    public TarjanSCC(DetailedMetrics metrics) {
        this.metrics = metrics;
    }

    public List<List<Integer>> findSCC(Map<Integer, List<Integer>> graph) {
        metrics.start();
        this.graph = graph;
        this.index = 0;
        this.indexes = new HashMap<>();
        this.lowlink = new HashMap<>();
        this.stack = new Stack<>();
        this.onStack = new HashSet<>();
        this.sccList = new ArrayList<>();
        this.nodeToComponent = new HashMap<>();

        for (int node : graph.keySet()) {
            metrics.incrementOps(); // Node visit
            if (!indexes.containsKey(node)) {
                strongConnect(node);
            }
        }

        metrics.stop();
        return sccList;
    }

    private void strongConnect(int v) {
        indexes.put(v, index);
        lowlink.put(v, index);
        index++;
        stack.push(v);
        onStack.add(v);

        // Explore neighbors
        for (int w : graph.getOrDefault(v, Collections.emptyList())) {
            metrics.incrementOps(); // Edge processing
            if (!indexes.containsKey(w)) {
                strongConnect(w);
                lowlink.put(v, Math.min(lowlink.get(v), lowlink.get(w)));
            } else if (onStack.contains(w)) {
                lowlink.put(v, Math.min(lowlink.get(v), indexes.get(w)));
            }
        }

        if (lowlink.get(v).equals(indexes.get(v))) {
            List<Integer> component = new ArrayList<>();
            int w;
            do {
                w = stack.pop();
                onStack.remove(w);
                component.add(w);
                nodeToComponent.put(w, sccList.size());
            } while (w != v);
            sccList.add(component);
        }
    }

    public Map<Integer, Integer> getNodeToComponentMap() {
        return Collections.unmodifiableMap(nodeToComponent);
    }

    public Map<Integer, List<Integer>> buildCondensationGraph(Map<Integer, List<Integer>> originalGraph) {
        Map<Integer, List<Integer>> condensation = new HashMap<>();

        // Initialize components
        for (int i = 0; i < sccList.size(); i++) {
            condensation.put(i, new ArrayList<>());
        }

        // Add edges between different components
        for (int u : originalGraph.keySet()) {
            int compU = nodeToComponent.get(u);
            for (int v : originalGraph.get(u)) {
                metrics.incrementOps(); // Edge processing
                int compV = nodeToComponent.get(v);
                if (compU != compV && !condensation.get(compU).contains(compV)) {
                    condensation.get(compU).add(compV);
                }
            }
        }

        return condensation;
    }
}