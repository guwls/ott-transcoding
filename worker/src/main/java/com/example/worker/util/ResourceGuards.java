package com.example.worker.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ResourceGuards {
    private static final Queue<Path> TEMP_TRACKER = new ConcurrentLinkedQueue<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            TEMP_TRACKER.forEach(ResourceGuards::rmRfQuietly);
        }, "temp-cleanup"));
    }

    public static Path tempDir(String prefix) {
        try {
            Path dir = Files.createTempDirectory(prefix);
            TEMP_TRACKER.add(dir);
            return dir;
        } catch (IOException e) { throw new RuntimeException("TEMP_DIR_CREATE_FAILED", e); }
    }

    public static Path tempFile(String prefix, String suffix) {
        try {
            Path f = Files.createTempFile(prefix, suffix);
            TEMP_TRACKER.add(f);
            return f;
        } catch (IOException e) { throw new RuntimeException("TEMP_FILE_CREATE_FAILED", e); }
    }

    public static void rmQuietly(Path p) { rmRfQuietly(p); }
    private static void rmRfQuietly(Path p) {
        if (p == null) return;
        try {
            if (Files.isDirectory(p)) {
                Files.walk(p).sorted((a,b)->b.compareTo(a)).forEach(x -> {
                    try { Files.deleteIfExists(x); } catch (Exception ignored) {}
                });
            } else Files.deleteIfExists(p);
        } catch (Exception ignored) {}
    }
}
