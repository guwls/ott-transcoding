package com.example.worker.ffmpeg;

import org.junit.jupiter.api.Test;
import java.nio.file.*;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;

class HlsMasterBuilderTest {
    @Test
    void build_master_includes_all_variants() throws Exception {
        var tmp = Files.createTempFile("master-", ".m3u8");
        var presets = List.of(
                new HlsPreset("480p", 854, 480, "1400k","1600k","2800k","96k"),
                new HlsPreset("720p",1280, 720, "2800k","3000k","5600k","128k"),
                new HlsPreset("1080p",1920,1080, "5000k","5300k","10000k","192k")
        );
        HlsMasterBuilder.writeMaster(tmp, presets);
        var s = Files.readString(tmp);
        assertThat(s).contains("480p/index.m3u8","720p/index.m3u8","1080p/index.m3u8");
        Files.deleteIfExists(tmp);
    }
}
