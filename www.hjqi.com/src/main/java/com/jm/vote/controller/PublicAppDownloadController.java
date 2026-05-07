package com.jm.vote.controller;

import com.jm.vote.dto.AppDownloadDTO;
import com.jm.vote.service.AppDownloadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 公开的App下载接口（无需认证）
 */
@RestController
@RequestMapping("/api/public/app-download")
@RequiredArgsConstructor
public class PublicAppDownloadController {

    private final AppDownloadService appDownloadService;

    /**
     * 获取最新版本的App下载信息
     */
    @GetMapping("/latest")
    public ResponseEntity<AppDownloadDTO> getLatestAppDownload(
            @RequestParam(required = false, defaultValue = "ANDROID") String platform) {
        return ResponseEntity.ok(appDownloadService.getLatestAppDownload(platform));
    }
}

