package graph.utils;

public class SimpleMetrics {
    private long startTime;
    private long endTime;
    private long opsCount;

    public void start() {
        startTime = System.nanoTime();
        opsCount = 0;
    }

    public void stop() {
        endTime = System.nanoTime();
    }

    public void incrementOps() {
        opsCount++;
    }

    public long getTimeNano() {
        return endTime - startTime;
    }

    public long getOpsCount() {
        return opsCount;
    }
}