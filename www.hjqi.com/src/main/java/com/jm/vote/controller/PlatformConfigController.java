package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.PlatformConfigDTO;
import com.jm.vote.service.PlatformConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/platform-config")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class PlatformConfigController {

    private final PlatformConfigService platformConfigService;

    /**
     * 获取平台配置列表
     */
    @GetMapping
    public ResponseEntity<Page<PlatformConfigDTO>> getConfigs(
            @RequestParam(required = false) String configType,
            @RequestParam(required = false) String configGroup,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(platformConfigService.getConfigs(configType, configGroup, page, size));
    }

    /**
     * 根据配置键获取配置
     */
    @GetMapping("/key/{configKey}")
    public ResponseEntity<PlatformConfigDTO> getConfigByKey(@PathVariable String configKey) {
        return ResponseEntity.ok(platformConfigService.getConfigByKey(configKey));
    }

    /**
     * 创建配置
     */
    @PostMapping
    public ResponseEntity<PlatformConfigDTO> createConfig(@RequestBody PlatformConfigDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(platformConfigService.createConfig(dto));
    }

    /**
     * 更新配置
     */
    @PutMapping("/{id}")
    public ResponseEntity<PlatformConfigDTO> updateConfig(
            @PathVariable Long id,
            @RequestBody PlatformConfigDTO dto) {
        return ResponseEntity.ok(platformConfigService.updateConfig(id, dto));
    }

    /**
     * 删除配置
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteConfig(@PathVariable Long id) {
        platformConfigService.deleteConfig(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }
}

