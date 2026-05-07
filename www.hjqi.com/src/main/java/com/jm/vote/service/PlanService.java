package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.controller.PlanController;
import com.jm.vote.entity.MerchantSubscription;
import com.jm.vote.entity.Plan;
import com.jm.vote.repository.MerchantSubscriptionMapper;
import com.jm.vote.repository.PlanMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanMapper planMapper;
    private final MerchantSubscriptionMapper subscriptionMapper;

    /**
     * 获取所有套餐
     */
    public List<Plan> getAllPlans() {
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plan::getStatus, 1)
                .eq(Plan::getDeleted, 0)
                .orderByAsc(Plan::getSort);
        
        return planMapper.selectList(wrapper);
    }

    /**
     * 商家订阅套餐
     */
    @Transactional
    public MerchantSubscription subscribePlan(Long merchantId, Long planId, Boolean autoRenew) {
        Plan plan = planMapper.selectById(planId);
        if (plan == null || plan.getStatus() != 1) {
            throw new RuntimeException("套餐不存在或已禁用");
        }
        
        // 取消之前的订阅
        LambdaQueryWrapper<MerchantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantSubscription::getMerchantId, merchantId)
                .eq(MerchantSubscription::getStatus, "ACTIVE");
        
        List<MerchantSubscription> activeSubscriptions = subscriptionMapper.selectList(wrapper);
        for (MerchantSubscription sub : activeSubscriptions) {
            sub.setStatus("CANCELLED");
            sub.setUpdatedAt(LocalDateTime.now());
            subscriptionMapper.updateById(sub);
        }
        
        // 创建新订阅
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(plan.getDurationDays());
        
        MerchantSubscription subscription = MerchantSubscription.builder()
                .merchantId(merchantId)
                .planId(planId)
                .startDate(startDate)
                .endDate(endDate)
                .status("ACTIVE")
                .autoRenew(autoRenew != null && autoRenew ? 1 : 0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        subscriptionMapper.insert(subscription);
        return subscription;
    }

    /**
     * 获取商家当前订阅
     */
    public MerchantSubscription getCurrentSubscription(Long merchantId) {
        LambdaQueryWrapper<MerchantSubscription> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantSubscription::getMerchantId, merchantId)
                .eq(MerchantSubscription::getStatus, "ACTIVE")
                .ge(MerchantSubscription::getEndDate, LocalDate.now())
                .orderByDesc(MerchantSubscription::getCreatedAt)
                .last("LIMIT 1");
        
        return subscriptionMapper.selectOne(wrapper);
    }

    /**
     * 计算服务费
     */
    public java.math.BigDecimal calculateServiceFee(Long merchantId) {
        MerchantSubscription subscription = getCurrentSubscription(merchantId);
        if (subscription == null) {
            return java.math.BigDecimal.ZERO;
        }
        
        Plan plan = planMapper.selectById(subscription.getPlanId());
        if (plan == null) {
            return java.math.BigDecimal.ZERO;
        }
        
        return plan.getPrice();
    }

    // ==================== 管理员方法 ====================

    /**
     * 获取所有套餐（管理员，包括禁用的）
     */
    public List<Plan> getAllPlansForAdmin() {
        LambdaQueryWrapper<Plan> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Plan::getDeleted, 0)
                .orderByAsc(Plan::getSort);
        return planMapper.selectList(wrapper);
    }

    /**
     * 根据ID获取套餐
     */
    public Plan getPlanById(Long id) {
        Plan plan = planMapper.selectOne(
                new LambdaQueryWrapper<Plan>()
                        .eq(Plan::getId, id)
                        .eq(Plan::getDeleted, 0)
        );
        return plan;
    }

    /**
     * 创建套餐
     */
    @Transactional
    public Plan createPlan(PlanController.CreatePlanRequest request) {
        Plan plan = new Plan();
        plan.setNameZh(request.getNameZh());
        plan.setNameEn(request.getNameEn());
        plan.setDescriptionZh(request.getDescriptionZh());
        plan.setDescriptionEn(request.getDescriptionEn());
        plan.setPrice(request.getPrice());
        plan.setDurationDays(request.getDurationDays());
        plan.setMaxProducts(request.getMaxProducts());
        plan.setMaxOrdersPerMonth(request.getMaxOrdersPerMonth());
        plan.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        plan.setSort(request.getSort() != null ? request.getSort() : 0);
        plan.setDeleted(0);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());
        
        planMapper.insert(plan);
        return plan;
    }

    /**
     * 更新套餐
     */
    @Transactional
    public Plan updatePlan(Long id, PlanController.UpdatePlanRequest request) {
        Plan plan = getPlanById(id);
        if (plan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "套餐不存在");
        }
        
        if (request.getNameZh() != null) plan.setNameZh(request.getNameZh());
        if (request.getNameEn() != null) plan.setNameEn(request.getNameEn());
        if (request.getDescriptionZh() != null) plan.setDescriptionZh(request.getDescriptionZh());
        if (request.getDescriptionEn() != null) plan.setDescriptionEn(request.getDescriptionEn());
        if (request.getPrice() != null) plan.setPrice(request.getPrice());
        if (request.getDurationDays() != null) plan.setDurationDays(request.getDurationDays());
        if (request.getMaxProducts() != null) plan.setMaxProducts(request.getMaxProducts());
        if (request.getMaxOrdersPerMonth() != null) plan.setMaxOrdersPerMonth(request.getMaxOrdersPerMonth());
        if (request.getStatus() != null) plan.setStatus(request.getStatus());
        if (request.getSort() != null) plan.setSort(request.getSort());
        plan.setUpdatedAt(LocalDateTime.now());
        
        planMapper.updateById(plan);
        return plan;
    }

    /**
     * 删除套餐（软删除）
     */
    @Transactional
    public void deletePlan(Long id) {
        Plan plan = getPlanById(id);
        if (plan == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "套餐不存在");
        }
        
        plan.setDeleted(1);
        plan.setUpdatedAt(LocalDateTime.now());
        planMapper.updateById(plan);
    }
}

