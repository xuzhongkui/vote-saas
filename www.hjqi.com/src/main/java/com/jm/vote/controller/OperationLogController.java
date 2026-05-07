package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.OperationLogDTO;
import com.jm.vote.service.OperationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/logs")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class OperationLogController {

    private final OperationLogService logService;

    @GetMapping
    public ResponseEntity<Page<OperationLogDTO>> getLogs(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String module,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(logService.getLogs(userType, module, page, size));
    }
}

