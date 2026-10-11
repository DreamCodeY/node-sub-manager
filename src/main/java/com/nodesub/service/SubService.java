package com.nodesub.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.SecureRandom;

/**
 * 订阅文件存取：只存用户主动上传的 YAML，不存任何解析过程数据。
 */
@Service
public class SubService {

    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();
    public static final int MAX_YAML_BYTES = 200 * 1024; // 200KB

    private final Path dataDir;

    public SubService(@Value("${app.data-dir:./data}") String dataDir) throws IOException {
        this.dataDir = Paths.get(dataDir).toAbsolutePath().normalize();
        Files.createDirectories(this.dataDir);
    }

    public static boolean isValidId(String id) {
        return id != null && id.matches("[A-Za-z0-9]{16}");
    }

    /** 保存 YAML，返回 16 位随机订阅 ID */
    public String save(String yaml) throws IOException {
        byte[] bytes = yaml.getBytes(StandardCharsets.UTF_8);
        if (bytes.length > MAX_YAML_BYTES) {
            throw new IllegalArgumentException("内容超过 200KB 上限");
        }
        String id = generateId();
        Files.write(dataDir.resolve(id + ".yaml"), bytes, StandardOpenOption.CREATE_NEW);
        return id;
    }

    public String load(String id) throws IOException {
        Path target = resolve(id);
        if (!Files.isRegularFile(target)) {
            throw new NotFoundException("订阅不存在或已被删除");
        }
        return Files.readString(target, StandardCharsets.UTF_8);
    }

    public void delete(String id) throws IOException {
        Path target = resolve(id);
        if (!Files.deleteIfExists(target)) {
            throw new NotFoundException("订阅不存在或已被删除");
        }
    }

    /** 校验 ID 格式并限定在 dataDir 内，防止路径穿越 */
    private Path resolve(String id) {
        if (!isValidId(id)) {
            throw new IllegalArgumentException("非法订阅 ID");
        }
        return dataDir.resolve(id + ".yaml").normalize();
    }

    private String generateId() {
        StringBuilder sb = new StringBuilder(16);
        for (int i = 0; i < 16; i++) {
            sb.append(ALPHABET.charAt(RANDOM.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }

    public static class NotFoundException extends RuntimeException {
        public NotFoundException(String message) {
            super(message);
        }
    }
}
