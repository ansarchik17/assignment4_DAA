package graph.utils;

public interface Metrics {
    void start();
    void stop();
    long getTimeNano();  //return time in nanoseconds

    void incrementOps(); //add one operation
    long getOpsCount();  //return count of operations

    void reset();
}