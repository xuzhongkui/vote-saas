package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.AppDownloadDTO;
import com.jm.vote.dto.CreateAppDownloadRequest;
import com.jm.vote.service.AppDownloadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/app-download")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AppDownloadController {

    private final AppDownloadService appDownloadService;

    /**
     * 获取App下载列表
     */
    @GetMapping
    public ResponseEntity<Page<AppDownloadDTO>> getAppDownloads(
            @RequestParam(required = false) String platform,
            @RequestParam(required = false) Integer enabled,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(appDownloadService.getAppDownloads(platform, enabled, page, size));
    }

    /**
     * 获取App下载详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<AppDownloadDTO> getAppDownload(@PathVariable Long id) {
        return ResponseEntity.ok(appDownloadService.getAppDownload(id));
    }

    /**
     * 创建App下载信息
     */
    @PostMapping
    public ResponseEntity<AppDownloadDTO> createAppDownload(@Valid @RequestBody CreateAppDownloadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(appDownloadService.createAppDownload(request));
    }

    /**
     * 更新App下载信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<AppDownloadDTO> updateAppDownload(
            @PathVariable Long id,
            @Valid @RequestBody CreateAppDownloadRequest request) {
        return ResponseEntity.ok(appDownloadService.updateAppDownload(id, request));
    }

    /**
     * 删除App下载信息
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteAppDownload(@PathVariable Long id) {
        appDownloadService.deleteAppDownload(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}

