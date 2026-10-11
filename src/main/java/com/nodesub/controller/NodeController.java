package com.nodesub.controller;

import com.nodesub.service.SubService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

/**
 * 只做订阅托管：解析与 YAML 生成全部在浏览器本地完成，
 * 服务端只存用户主动点击"生成订阅"时上传的 YAML。
 */
@RestController
public class NodeController {

    private final SubService subService;

    public NodeController(SubService subService) {
        this.subService = subService;
    }

    /** 用户主动生成订阅：body 为 YAML 纯文本 */
    @PostMapping(value = "/api/sub", consumes = MediaType.TEXT_PLAIN_VALUE)
    public Map<String, String> createSub(@RequestBody String yaml) throws IOException {
        if (yaml == null || yaml.isBlank()) {
            throw new IllegalArgumentException("内容不能为空");
        }
        String id = subService.save(yaml);
        return Map.of("id", id);
    }

    /** 订阅拉取：供 OpenClash / Clash 添加订阅 */
    @GetMapping(value = "/sub/{id}", produces = "text/yaml;charset=UTF-8")
    public ResponseEntity<String> getSub(@PathVariable String id) throws IOException {
        String yaml = subService.load(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/yaml;charset=UTF-8"))
                .body(yaml);
    }

    /** 删除订阅：用户可随时删除服务端数据 */
    @DeleteMapping("/sub/{id}")
    public Map<String, String> deleteSub(@PathVariable String id) throws IOException {
        subService.delete(id);
        return Map.of("deleted", id);
    }

    @ExceptionHandler(SubService.NotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(SubService.NotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<Map<String, String>> handleIo(IOException e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "服务端读写失败"));
    }
}
