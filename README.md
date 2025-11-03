# 🏙️ Assignment 4 — Strongly Connected Components (SCC) & Topological Ordering

![Java](https://img.shields.io/badge/Java-17-blue?logo=java)
![Maven](https://img.shields.io/badge/Maven-Build-orange?logo=apache-maven)
![Algorithms](https://img.shields.io/badge/Algorithms-MST-success)
![Status](https://img.shields.io/badge/Status-Completed-brightgreen)

**Student:** Ansar Keles
**University:** Astana IT University  
**Course:** Design and Algorithms and Data Structures  
**Date:** October 2025

---

## 📘 Project Overview
Consolidate two fundamental graph algorithm topics by applying them to a practical smart city scheduling scenario:

1)Strongly Connected Components (SCC) & Topological Ordering
2)Shortest Paths in Directed Acyclic Graphs (DAGs)


---

## ⚙️ How to Run
1. Open the project in **IntelliJ IDEA** (as a Maven project)
2. Run the class:
   mst.Main
3. The program will generate:
- `analysis_results.csv` — summary table

---

## 📊 Experimental Results

<img width="1220" height="212" alt="Screenshot 2025-11-03 at 10 30 34" src="https://github.com/user-attachments/assets/e7e2744e-6a3a-4dba-890a-7fb1c1efdc4d" />

---

## 🧠 Analysis
SCC (Tarjan's Algorithm)

Bottlenecks:

DFS recursion stack depth for large graphs
Stack operations for cycle detection
Component identification and mapping
Effect of Structure:

Dense graphs: More DFS calls, higher operation count
Large SCCs: Increased stack usage, longer processing per component
Cyclic structures: More back edges to process, complex lowlink calculations
Performance Characteristics:

Time Complexity: O(V + E)
Space Complexity: O(V)
Best for: Detecting cycles, finding strongly connected components
Topological Sort (Kahn's Algorithm)

Bottlenecks:

In-degree calculation for all nodes
Queue operations for zero-in-degree nodes
Cycle detection overhead
Effect of Structure:

High density: More edges to process for in-degree calculation
Long chains: Sequential processing, limited parallelism
Multiple sources: Faster convergence with more initial zero-in-degree nodes
Performance Characteristics:

Time Complexity: O(V + E)
Space Complexity: O(V)
Best for: Dependency resolution, task scheduling
DAG Shortest/Longest Path

Bottlenecks:

Topological sort as prerequisite
Distance updates for all edges
Path reconstruction backtracking
Effect of Structure:

Dense DAGs: More edge relaxations, higher operation count
Long paths: More distance updates, longer path reconstruction
Multiple paths: Increased comparison operations for optimal path selection
Performance Characteristics:

Time Complexity: O(V + E)
Space Complexity: O(V)
Best for: Critical path analysis, task scheduling optimization

---

---

## 🧾 References
- GeeksForGeeks — *Tarjan's algorithm*
- Cormen et al., *Introduction to Algorithms (CLRS, 3rd Edition)*
- Astana IT University course materials

## 🧠 Analysis

Conclusions and Practical Recommendations

When to Use Each Method

SCC Detection (Tarjan's Algorithm):

Use when: You need to detect cycles, find strongly connected components, or compress cyclic dependencies
Avoid when: Graph is known to be acyclic (use topological sort directly)
Best for: Dependency analysis, cycle detection, graph compression
Topological Sort:

Use when: Tasks have dependencies and need execution ordering
Avoid when: Graph contains cycles (without SCC preprocessing)
Best for: Build systems, task scheduling, course prerequisites
DAG Shortest/Longest Path:

Use when: Need critical path analysis or optimal task scheduling
Avoid when: Graph has negative cycles (not applicable to DAGs)
Best for: Project management, critical path method, resource allocation
Performance Recommendations

For Small Graphs (<20 nodes):

All algorithms perform well
Prefer simplicity and code clarity
SCC preprocessing beneficial for unknown graph structures
For Medium Graphs (20-100 nodes):

Consider memory usage for SCC algorithm
Topological sort remains efficient
Path finding scales linearly with graph size
For Large Graphs (>100 nodes):

Monitor stack depth for SCC DFS
Consider iterative approaches for memory management
Batch processing for multiple source shortest paths
Structural Considerations

Dense Graphs:

Higher memory requirements
Consider edge pruning for very dense graphs
SCC compression significantly reduces problem size
Sparse Graphs:

Optimal performance for all algorithms
Minimal memory footprint
Fast path reconstruction
Mixed Structures:

SCC preprocessing essential
Condensation to DAG enables efficient path finding
Balanced performance characteristics
Implementation Insights

Memory Optimization:

Reuse data structures between algorithms
Use primitive collections for large graphs
Consider iterative DFS for deep graphs
Performance Optimization:

Cache topological order for multiple path computations
Precompute in-degrees for topological sort
Use efficient priority queues for generalized algorithms
Practical Applications:

Smart City Scheduling: Use critical path for resource allocation
Task Dependencies: Topological sort for execution order
System Dependencies: SCC for cycle detection and component isolation
This analysis demonstrates that the combination of SCC detection, topological sorting, and DAG path finding provides a comprehensive toolkit for analyzing complex dependency graphs in smart city scheduling scenarios. The algorithms scale well with graph size and provide valuable insights for optimal task scheduling and resource management.






