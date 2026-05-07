package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.entity.MerchantViolation;
import com.jm.vote.repository.MerchantViolationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MerchantViolationService {

    private final MerchantViolationMapper violationMapper;

    /**
     * 创建违规记录（管理员）
     */
    @Transactional
    public MerchantViolation createViolation(Long merchantId, String violationType, String title, 
                                            String description, String penaltyType, java.math.BigDecimal penaltyAmount, 
                                            Integer penaltyDurationDays) {
        MerchantViolation violation = MerchantViolation.builder()
                .merchantId(merchantId)
                .violationType(violationType)
                .title(title)
                .description(description)
                .penaltyType(penaltyType)
                .penaltyAmount(penaltyAmount)
                .penaltyDurationDays(penaltyDurationDays)
                .status("PENDING")
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        violationMapper.insert(violation);
        return violation;
    }

    /**
     * 处理违规记录（管理员）
     */
    @Transactional
    public void handleViolation(Long violationId, Long adminId, String status, String adminRemark) {
        MerchantViolation violation = violationMapper.selectById(violationId);
        if (violation == null) {
            throw new RuntimeException("违规记录不存在");
        }
        
        violation.setAdminId(adminId);
        violation.setStatus(status);
        violation.setAdminRemark(adminRemark);
        violation.setResolvedAt(LocalDateTime.now());
        violation.setUpdatedAt(LocalDateTime.now());
        
        violationMapper.updateById(violation);
        
        // 如果处罚类型是禁用，需要更新商家状态
        if ("SUSPEND".equals(violation.getPenaltyType()) || "BAN".equals(violation.getPenaltyType())) {
            // 这里可以调用MerchantService更新商家状态
        }
    }

    /**
     * 获取违规记录列表
     */
    public Page<MerchantViolation> getViolations(Long merchantId, String status, int page, int size) {
        LambdaQueryWrapper<MerchantViolation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantViolation::getDeleted, 0);
        
        if (merchantId != null) {
            wrapper.eq(MerchantViolation::getMerchantId, merchantId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(MerchantViolation::getStatus, status);
        }
        
        wrapper.orderByDesc(MerchantViolation::getCreatedAt);
        return violationMapper.selectPage(new Page<>(page, size), wrapper);
    }
}

