package com.jm.vote.controller;

import com.jm.vote.dto.FileUploadResponse;
import com.jm.vote.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/common")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("收到文件上传请求: {}, 大小: {} bytes", file.getOriginalFilename(), file.getSize());
            FileUploadResponse response = fileUploadService.uploadFile(file);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("文件上传参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("文件上传失败: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of("message", "文件上传失败: " + e.getMessage()));
        }
    }
}

