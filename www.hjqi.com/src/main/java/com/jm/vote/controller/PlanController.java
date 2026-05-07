package com.jm.vote.controller;

import com.jm.vote.entity.Plan;
import com.jm.vote.service.PlanService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/plans")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class PlanController {

    private final PlanService planService;

    /**
     * 获取所有套餐（管理员）
     */
    @GetMapping
    public ResponseEntity<List<Plan>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlansForAdmin());
    }

    /**
     * 获取套餐详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<Plan> getPlan(@PathVariable Long id) {
        Plan plan = planService.getPlanById(id);
        if (plan == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(plan);
    }

    /**
     * 创建套餐
     */
    @PostMapping
    public ResponseEntity<Plan> createPlan(@RequestBody CreatePlanRequest request) {
        Plan plan = planService.createPlan(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(plan);
    }

    /**
     * 更新套餐
     */
    @PutMapping("/{id}")
    public ResponseEntity<Plan> updatePlan(@PathVariable Long id, @RequestBody UpdatePlanRequest request) {
        Plan plan = planService.updatePlan(id, request);
        return ResponseEntity.ok(plan);
    }

    /**
     * 删除套餐
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deletePlan(@PathVariable Long id) {
        planService.deletePlan(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    @Data
    public static class CreatePlanRequest {
        private String nameZh;
        private String nameEn;
        private String descriptionZh;
        private String descriptionEn;
        private java.math.BigDecimal price;
        private Integer durationDays;
        private Integer maxProducts;
        private Integer maxOrdersPerMonth;
        private Integer status;
        private Integer sort;
    }

    @Data
    public static class UpdatePlanRequest {
        private String nameZh;
        private String nameEn;
        private String descriptionZh;
        private String descriptionEn;
        private java.math.BigDecimal price;
        private Integer durationDays;
        private Integer maxProducts;
        private Integer maxOrdersPerMonth;
        private Integer status;
        private Integer sort;
    }
}

