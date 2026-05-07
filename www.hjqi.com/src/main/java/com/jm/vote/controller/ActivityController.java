package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.ActivityDTO;
import com.jm.vote.dto.CreateActivityRequest;
import com.jm.vote.dto.UpdateActivityRequest;
import com.jm.vote.service.ActivityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/activity")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class ActivityController {

    private final ActivityService activityService;

    /**
     * 获取活动列表
     */
    @GetMapping
    public ResponseEntity<Page<ActivityDTO>> getActivities(
            @RequestParam(required = false) String activityType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(activityService.getActivities(activityType, status, page, size));
    }

    /**
     * 获取活动详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ActivityDTO> getActivity(@PathVariable Long id) {
        return ResponseEntity.ok(activityService.getActivity(id));
    }

    /**
     * 创建活动
     */
    @PostMapping
    public ResponseEntity<ActivityDTO> createActivity(@Valid @RequestBody CreateActivityRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(activityService.createActivity(request));
    }

    /**
     * 更新活动
     */
    @PutMapping("/{id}")
    public ResponseEntity<ActivityDTO> updateActivity(
            @PathVariable Long id,
            @Valid @RequestBody UpdateActivityRequest request) {
        return ResponseEntity.ok(activityService.updateActivity(id, request));
    }

    /**
     * 删除活动
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteActivity(@PathVariable Long id) {
        activityService.deleteActivity(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}

