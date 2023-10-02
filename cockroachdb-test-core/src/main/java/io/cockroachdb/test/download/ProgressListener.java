package io.cockroachdb.test.download;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicLong;

import io.cockroachdb.test.download.http.HttpResponse;
import io.cockroachdb.test.util.ByteFormat;

public class ProgressListener implements DownloadListener {
    private static final double SMOOTHING_FACTOR = .6;

    private static final long SAMPLE_MILLIS = 1000;

    private static final int SAMPLE_LIMIT = 20;

    private final Deque<Long> sampleWindow = new ArrayDeque<>(SAMPLE_LIMIT);

    private final AtomicLong sampleTime = new AtomicLong();

    private final AtomicLong sampleBytes = new AtomicLong();

    private final AtomicLong totalBytesReceived = new AtomicLong();

    private final ProgressCallback progressCallback;

    private AtomicLong totalBytes;

    private double averageSpeedBps = Double.NEGATIVE_INFINITY;

    public ProgressListener(ProgressCallback progressCallback) {
        this.progressCallback = progressCallback;
    }

    @Override
    public void beforeDownload(HttpResponse response) {
        this.totalBytes = new AtomicLong(response.getContentLength());
        this.sampleTime.set(System.currentTimeMillis());
        this.sampleBytes.set(0);
    }

    @Override
    public void bytesRead(HttpResponse response, byte[] buffer, int bytesRead) {
        this.totalBytesReceived.addAndGet(bytesRead);

        long time = System.currentTimeMillis() - this.sampleTime.get();
        if (time > SAMPLE_MILLIS) {
            takeSnapshot(time);
            sampleTime.set(System.currentTimeMillis());
            sampleBytes.set(totalBytesReceived.get());
        }
    }

    @Override
    public void afterDownload(HttpResponse response) {
        takeSnapshot(System.currentTimeMillis() - this.sampleTime.get());
    }

    private void takeSnapshot(long sampleMillis) {
        long snapshotSize = totalBytesReceived.get() - sampleBytes.get();
        if (snapshotSize <= 0) {
            return;
        }

        while (sampleWindow.size() > SAMPLE_LIMIT) {
            sampleWindow.removeFirst();
        }
        sampleWindow.addLast(snapshotSize);

        // Calculate transfer speed in bytes per second using sample average
        double sampleAvg = sampleWindow.stream().mapToLong((x) -> x).summaryStatistics().getAverage();
        double sampleSpeedBps = sampleAvg / (sampleMillis / 1000.0);

        // Calculate estimated time remaining in seconds using exponential moving average.
        // http://en.wikipedia.org/wiki/Moving_average#Exponential_moving_average
        if (this.averageSpeedBps < 0) {
            this.averageSpeedBps = sampleSpeedBps;
        }

        this.averageSpeedBps = SMOOTHING_FACTOR * sampleSpeedBps + (1 - SMOOTHING_FACTOR) * this.averageSpeedBps;

        if (averageSpeedBps > 0) {
            double progress = totalBytes.get() > 0
                    ? (totalBytesReceived.get() + .0) / (totalBytes.get() + .0) * 100.0
                    : .0;

            double speedMBps = averageSpeedBps / (double) ByteFormat.ONE_MB;

            long remainingTime = averageSpeedBps > 0
                    ? (long) ((totalBytes.get() - totalBytesReceived.get()) / averageSpeedBps * 1000)
                    : 0;

            progressCallback.print(progress, speedMBps, totalBytesReceived.get(), remainingTime);
        }
    }
}
