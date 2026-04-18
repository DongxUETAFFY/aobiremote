package io.github.dongxuetaffy.aobihelper.common.metrics;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLongArray;
import org.springframework.stereotype.Component;

@Component
public class RequestMetricsService {
    private static final int WINDOW_SECONDS = 60;

    private final AtomicLongArray bucketSeconds = new AtomicLongArray(WINDOW_SECONDS);
    private final AtomicLongArray bucketCounts = new AtomicLongArray(WINDOW_SECONDS);

    public void recordRequest() {
        long currentSecond = Instant.now().getEpochSecond();
        int slot = (int) (currentSecond % WINDOW_SECONDS);
        ensureCurrentBucket(slot, currentSecond);
        bucketCounts.incrementAndGet(slot);
    }

    public double getRecentQps() {
        long currentSecond = Instant.now().getEpochSecond();
        long windowStart = currentSecond - WINDOW_SECONDS + 1;
        long totalRequests = 0L;
        for (int i = 0; i < WINDOW_SECONDS; i++) {
            long bucketSecond = bucketSeconds.get(i);
            if (bucketSecond >= windowStart && bucketSecond <= currentSecond) {
                totalRequests += bucketCounts.get(i);
            }
        }
        return totalRequests / (double) WINDOW_SECONDS;
    }

    private void ensureCurrentBucket(int slot, long currentSecond) {
        if (bucketSeconds.get(slot) == currentSecond) {
            return;
        }
        synchronized (this) {
            if (bucketSeconds.get(slot) != currentSecond) {
                bucketSeconds.set(slot, currentSecond);
                bucketCounts.set(slot, 0L);
            }
        }
    }
}
