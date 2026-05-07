package com.jm.vote.controller;

import com.jm.vote.entity.MerchantNotificationConfig;
import com.jm.vote.service.MerchantNotificationConfigService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant/notification-configs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class MerchantNotificationConfigController {

    private final MerchantNotificationConfigService notificationConfigService;
    private final SecurityUtil securityUtil;

    /**
     * 获取通知配置列表
     */
    @GetMapping
    public ResponseEntity<List<MerchantNotificationConfig>> getConfigs() {
        Long merchantId = securityUtil.getCurrentMerchantId();
        List<MerchantNotificationConfig> configs = notificationConfigService.getConfigs(merchantId);
        return ResponseEntity.ok(configs);
    }

    /**
     * 获取通知配置（按类型分组）
     */
    @GetMapping("/grouped")
    public ResponseEntity<Map<String, List<MerchantNotificationConfig>>> getConfigsGrouped() {
        Long merchantId = securityUtil.getCurrentMerchantId();
        Map<String, List<MerchantNotificationConfig>> configs = notificationConfigService.getConfigsGrouped(merchantId);
        return ResponseEntity.ok(configs);
    }

    /**
     * 保存通知配置
     */
    @PostMapping
    public ResponseEntity<MerchantNotificationConfig> saveConfig(@RequestBody SaveNotificationConfigRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        MerchantNotificationConfig config = notificationConfigService.saveConfig(
                merchantId,
                request.getNotificationType(),
                request.getNotificationChannel(),
                request.getEnabled(),
                request.getConfigJson()
        );
        return ResponseEntity.ok(config);
    }

    /**
     * 批量保存通知配置
     */
    @PostMapping("/batch")
    public ResponseEntity<Map<String, String>> saveConfigs(@RequestBody List<SaveNotificationConfigRequest> requests) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        // 转换为Service需要的类型
        List<MerchantNotificationConfigService.SaveNotificationConfigRequest> serviceRequests = requests.stream()
                .map(req -> {
                    MerchantNotificationConfigService.SaveNotificationConfigRequest serviceReq = 
                            new MerchantNotificationConfigService.SaveNotificationConfigRequest();
                    serviceReq.setNotificationType(req.getNotificationType());
                    serviceReq.setNotificationChannel(req.getNotificationChannel());
                    serviceReq.setEnabled(req.getEnabled());
                    serviceReq.setConfigJson(req.getConfigJson());
                    return serviceReq;
                })
                .collect(java.util.stream.Collectors.toList());
        notificationConfigService.saveConfigs(merchantId, serviceRequests);
        return ResponseEntity.ok(Map.of("message", "保存成功"));
    }

    /**
     * 删除通知配置
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteConfig(@PathVariable Long id) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        notificationConfigService.deleteConfig(id, merchantId);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // 内部请求类
    @lombok.Data
    public static class SaveNotificationConfigRequest {
        private String notificationType;
        private String notificationChannel;
        private Integer enabled;
        private String configJson;
    }
}

