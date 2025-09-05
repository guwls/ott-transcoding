package com.example.worker.storage;

import io.minio.*;
import io.minio.errors.MinioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.util.Objects;

@Component
public class StorageService {
    private final MinioClient minio;
    private final String bucket;

    public StorageService(MinioClient minio, @Value("${storage.bucket}") String bucket) {
        this.minio = minio; this.bucket = bucket;
    }

    /** objectKey를 로컬 temp 파일로 다운로드 */
    public Path downloadToTemp(String objectKey) {
        try {
            Path tmp = Files.createTempFile("input-", "-" + Paths.get(objectKey).getFileName());
            try (GetObjectResponse obj = minio.getObject(GetObjectArgs.builder().bucket(bucket).object(objectKey).build());
                 OutputStream out = Files.newOutputStream(tmp, StandardOpenOption.WRITE)) {
                obj.transferTo(out);
            }
            return tmp;
        } catch (Exception e) {
            throw new RuntimeException("DOWNLOAD_FAILED: " + objectKey, e);
        }
    }

    /** 디렉토리 내 모든 파일을 prefix 아래로 업로드 */
    public void uploadDir(String prefix, Path dir) {
        try {
            Files.walk(dir).filter(Files::isRegularFile).forEach(path -> {
                String rel = dir.relativize(path).toString().replace("\\","/");
                String key = (prefix + "/" + rel).replaceAll("//+", "/");
                String ct = contentTypeFor(path.getFileName().toString());
                try (InputStream in = Files.newInputStream(path)) {
                    minio.putObject(PutObjectArgs.builder()
                            .bucket(bucket).object(key)
                            .contentType(ct)
                            .stream(in, Files.size(path), -1)
                            .build());
                } catch (Exception ex) {
                    throw new RuntimeException("UPLOAD_FAILED: " + key, ex);
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("UPLOAD_DIR_FAILED: " + dir, e);
        }
    }

    private String contentTypeFor(String name) {
        String lower = name.toLowerCase();
        if (lower.endsWith(".m3u8")) return "application/vnd.apple.mpegurl";
        if (lower.endsWith(".ts"))   return "video/MP2T";
        if (lower.endsWith(".mp4"))  return "video/mp4";
        return "application/octet-stream";
    }
}