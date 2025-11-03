package graph.utils;

public class DetailedMetrics implements Metrics {
    private long startTime;
    private long endTime;
    private long opsCount;
    private long memoryBefore;
    private long memoryAfter;

    @Override
    public void start() {
        System.gc();
        this.memoryBefore = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        this.startTime = System.nanoTime();
        this.opsCount = 0;
    }

    @Override
    public void stop() {
        this.endTime = System.nanoTime();
        System.gc();
        this.memoryAfter = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    @Override
    public long getTimeNano() {
        return endTime - startTime;
    }

    @Override
    public void incrementOps() {
        opsCount++;
    }

    @Override
    public long getOpsCount() {
        return opsCount;
    }

    @Override
    public void reset() {
        startTime = 0;
        endTime = 0;
        opsCount = 0;
    }

    public long getMemoryUsed() {
        return memoryAfter - memoryBefore;
    }

    public double getTimeMs() {
        return getTimeNano() / 1_000_000.0;
    }
}