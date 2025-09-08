package com.example.worker.util;

import java.io.File;

public class SystemGuards {
    public static void requireDiskSpace(File path, long minBytes) {
        long free = path.getUsableSpace();
        if (free < minBytes) throw new RuntimeException("LOW_DISK("+free+"B)");
    }
    public static void requireFreeMemory(long minBytes) {
        long free = Runtime.getRuntime().freeMemory();
        long max  = Runtime.getRuntime().maxMemory();
        if (free + (max - Runtime.getRuntime().totalMemory()) < minBytes)
            throw new RuntimeException("LOW_HEAP");
    }
}
