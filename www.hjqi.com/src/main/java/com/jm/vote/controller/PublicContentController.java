package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.ContentDTO;
import com.jm.vote.service.ContentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/content")
@RequiredArgsConstructor
public class PublicContentController {

    private final ContentService contentService;

    @GetMapping
    public ResponseEntity<Page<ContentDTO>> getContents(
            @RequestParam(required = false) String contentType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        // 公开接口只返回已发布的内容
        return ResponseEntity.ok(contentService.getContents(contentType, page, size, 1));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentDTO> getContent(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.getContent(id));
    }
}

