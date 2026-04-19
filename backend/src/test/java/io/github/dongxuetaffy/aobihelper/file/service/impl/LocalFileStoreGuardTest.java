package io.github.dongxuetaffy.aobihelper.file.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LocalFileStoreGuardTest {
    @Test
    void executeLimitsConcurrentFileStoreWork() throws Exception {
        LocalFileStoreGuard guard = new LocalFileStoreGuard(1, path -> {
            return path;
        });
        CountDownLatch firstEntered = new CountDownLatch(1);
        CountDownLatch releaseFirst = new CountDownLatch(1);
        CountDownLatch secondEntered = new CountDownLatch(1);

        var executor = Executors.newFixedThreadPool(2);
        try {
            Future<?> firstUpload = executor.submit(() -> executeGuarded(guard, () -> {
                firstEntered.countDown();
                await(releaseFirst);
            }));
            assertThat(firstEntered.await(1, TimeUnit.SECONDS)).isTrue();

            Future<?> secondUpload = executor.submit(() -> executeGuarded(guard, secondEntered::countDown));

            assertThat(secondEntered.await(150, TimeUnit.MILLISECONDS)).isFalse();
            releaseFirst.countDown();
            assertThat(secondEntered.await(1, TimeUnit.SECONDS)).isTrue();
            firstUpload.get(1, TimeUnit.SECONDS);
            secondUpload.get(1, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void ensureDirectoryExistsCachesSuccessfulDirectoryCreation() throws Exception {
        AtomicInteger createCount = new AtomicInteger();
        LocalFileStoreGuard guard = new LocalFileStoreGuard(2, path -> {
            createCount.incrementAndGet();
            return path;
        });
        Path directory = Path.of("target/aobi-file-guard-test").toAbsolutePath().normalize();

        guard.ensureDirectoryExists(directory);
        guard.ensureDirectoryExists(directory);

        assertThat(createCount).hasValue(1);
    }

    private void executeGuarded(LocalFileStoreGuard guard, LocalFileStoreGuard.FileStoreOperation operation) {
        try {
            guard.execute(operation);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private void await(CountDownLatch latch) {
        try {
            latch.await(2, TimeUnit.SECONDS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }
}
