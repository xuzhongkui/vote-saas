package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.ContentDTO;
import com.jm.vote.dto.CreateContentRequest;
import com.jm.vote.dto.UpdateContentRequest;
import com.jm.vote.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/content")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ContentController {

    private final ContentService contentService;

    @GetMapping
    public ResponseEntity<Page<ContentDTO>> getContents(
            @RequestParam(required = false) String contentType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(contentService.getContents(contentType, page, size, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ContentDTO> getContent(@PathVariable Long id) {
        return ResponseEntity.ok(contentService.getContent(id));
    }

    @PostMapping
    public ResponseEntity<ContentDTO> createContent(@Valid @RequestBody CreateContentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(contentService.createContent(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ContentDTO> updateContent(
            @PathVariable Long id,
            @Valid @RequestBody UpdateContentRequest request) {
        return ResponseEntity.ok(contentService.updateContent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteContent(@PathVariable Long id) {
        contentService.deleteContent(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}

