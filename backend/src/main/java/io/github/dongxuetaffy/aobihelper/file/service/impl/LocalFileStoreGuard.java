package io.github.dongxuetaffy.aobihelper.file.service.impl;

import io.github.dongxuetaffy.aobihelper.common.constant.BusinessCode;
import io.github.dongxuetaffy.aobihelper.common.exception.BusinessException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Semaphore;

final class LocalFileStoreGuard {
    private final Semaphore fileStorePermits;
    private final DirectoryCreator directoryCreator;
    private final ConcurrentMap<Path, Boolean> createdDirectories = new ConcurrentHashMap<>();

    LocalFileStoreGuard(int maxConcurrentStores) {
        this(maxConcurrentStores, Files::createDirectories);
    }

    LocalFileStoreGuard(int maxConcurrentStores, DirectoryCreator directoryCreator) {
        this.fileStorePermits = new Semaphore(Math.max(1, maxConcurrentStores), true);
        this.directoryCreator = directoryCreator;
    }

    void execute(FileStoreOperation operation) throws IOException {
        acquirePermit();
        try {
            operation.run();
        } finally {
            fileStorePermits.release();
        }
    }

    void ensureDirectoryExists(Path directory) throws IOException {
        Path normalizedDirectory = directory.toAbsolutePath().normalize();
        if (createdDirectories.putIfAbsent(normalizedDirectory, Boolean.TRUE) != null) {
            return;
        }
        try {
            directoryCreator.createDirectories(normalizedDirectory);
        } catch (IOException | RuntimeException exception) {
            createdDirectories.remove(normalizedDirectory);
            throw exception;
        }
    }

    void forgetDirectory(Path directory) {
        createdDirectories.remove(directory.toAbsolutePath().normalize());
    }

    private void acquirePermit() {
        try {
            fileStorePermits.acquire();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(BusinessCode.UPLOAD_FAILED, "Image upload was interrupted");
        }
    }

    @FunctionalInterface
    interface FileStoreOperation {
        void run() throws IOException;
    }

    @FunctionalInterface
    interface DirectoryCreator {
        Path createDirectories(Path directory) throws IOException;
    }
}
