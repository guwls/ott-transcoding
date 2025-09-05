package com.example.worker.ffmpeg;

import java.util.*;

public class PresetFactory {

    private static final Map<String, HlsPreset> PRESETS = Map.of(
            "480p",  new HlsPreset("480p",  854,  480, "1400k", "1600k", "2800k", "96k"),
            "720p",  new HlsPreset("720p", 1280,  720, "2800k", "3000k", "5600k", "128k"),
            "1080p", new HlsPreset("1080p",1920, 1080, "5000k", "5300k", "10000k", "192k")
    );

    public static Optional<HlsPreset> find(String name) {
        if (name == null) return Optional.empty();
        var key = name.toLowerCase();
        // "720p" / "720P" 등 허용
        return PRESETS.keySet().stream()
                .filter(k -> k.equalsIgnoreCase(key))
                .findFirst()
                .map(PRESETS::get);
    }

    public static List<HlsPreset> resolveAll(List<String> names) {
        if (names == null || names.isEmpty()) return List.of(PRESETS.get("480p"), PRESETS.get("720p"), PRESETS.get("1080p"));
        List<HlsPreset> out = new ArrayList<>();
        for (String n : names) find(n).ifPresent(out::add);
        return out;
    }
}