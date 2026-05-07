package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.annotation.RequirePayment;
import com.jm.vote.dto.PromotionRecordDTO;
import com.jm.vote.dto.PromotionStatisticsDTO;
import com.jm.vote.entity.PromotionWithdrawal;
import com.jm.vote.service.PromotionService;
import com.jm.vote.util.SecurityUtil;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 推广管理接口
 */
@Slf4j
@RestController
@RequestMapping("/api/merchant/promotion")
@RequiredArgsConstructor
@RequirePayment // 推广功能需要缴费后才能使用
public class PromotionController {

    private final PromotionService promotionService;
    private final SecurityUtil securityUtil;

    /**
     * 获取推广链接
     */
    @GetMapping("/link")
    public ResponseEntity<Map<String, Object>> getPromotionLink() {
        Long merchantId = securityUtil.requireMerchant();
        String link = promotionService.generatePromotionLink(merchantId);
        return ResponseEntity.ok(Map.of("promotionLink", link));
    }

    /**
     * 获取推广统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<PromotionStatisticsDTO> getPromotionStatistics() {
        Long merchantId = securityUtil.requireMerchant();
        PromotionStatisticsDTO statistics = promotionService.getPromotionStatistics(merchantId);
        return ResponseEntity.ok(statistics);
    }

    /**
     * 获取推广记录列表
     */
    @GetMapping("/records")
    public ResponseEntity<Page<PromotionRecordDTO>> getPromotionRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long merchantId = securityUtil.requireMerchant();
        Page<PromotionRecordDTO> records = promotionService.getPromotionRecords(merchantId, page, size);
        return ResponseEntity.ok(records);
    }

    /**
     * 申请提现
     */
    @PostMapping("/withdrawal")
    public ResponseEntity<Map<String, Object>> applyWithdrawal(@RequestBody WithdrawalRequest request) {
        Long merchantId = securityUtil.requireMerchant();
        
        Map<String, Object> result = promotionService.applyWithdrawal(
                merchantId,
                request.getAmount(),
                request.getAccountType(),
                request.getAccountName(),
                request.getAccountNumber()
        );
        
        return ResponseEntity.ok(result);
    }

    /**
     * 获取提现记录列表
     */
    @GetMapping("/withdrawals")
    public ResponseEntity<List<PromotionWithdrawal>> getWithdrawalRecords() {
        Long merchantId = securityUtil.requireMerchant();
        List<PromotionWithdrawal> records = promotionService.getWithdrawalRecords(merchantId);
        return ResponseEntity.ok(records);
    }

    @Data
    public static class WithdrawalRequest {
        private BigDecimal amount;
        private String accountType;
        private String accountName;
        private String accountNumber;
    }
}
