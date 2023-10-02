package io.cockroachdb.test.download;

@FunctionalInterface
public interface ProgressCallback {
    void print(double progress,
               double averageSpeedBps,
               long totalBytes,
               long remainingMillis);
}
