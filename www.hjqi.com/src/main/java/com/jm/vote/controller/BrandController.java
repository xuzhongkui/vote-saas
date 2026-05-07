package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.entity.Brand;
import com.jm.vote.service.BrandService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/brand")
@RequiredArgsConstructor
public class BrandController {

    private final BrandService brandService;
    private final SecurityUtil securityUtil;

    /**
     * 创建品牌
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> createBrand(
            HttpServletRequest request,
            @RequestParam String nameZh,
            @RequestParam(required = false) String nameEn,
            @RequestParam(required = false) String logo,
            @RequestParam(required = false) String descriptionZh,
            @RequestParam(required = false) String descriptionEn) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Brand brand = brandService.createBrand(merchantId, nameZh, nameEn, logo, descriptionZh, descriptionEn);
        
        Map<String, Object> response = new HashMap<>();
        response.put("brandId", brand.getId());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取品牌列表
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<Brand>> getBrands(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<Brand> brands = brandService.getBrands(merchantId, page, size);
        return ResponseEntity.ok(brands);
    }

    /**
     * 获取所有品牌（不分页）
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<List<Brand>> getAllBrands(HttpServletRequest request) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        List<Brand> brands = brandService.getAllBrands(merchantId);
        return ResponseEntity.ok(brands);
    }

    /**
     * 更新品牌
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> updateBrand(
            @PathVariable Long id,
            @RequestParam(required = false) String nameZh,
            @RequestParam(required = false) String nameEn,
            @RequestParam(required = false) String logo,
            @RequestParam(required = false) String descriptionZh,
            @RequestParam(required = false) String descriptionEn,
            @RequestParam(required = false) Integer sort,
            @RequestParam(required = false) Integer status) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Brand brand = brandService.updateBrand(id, merchantId, nameZh, nameEn, logo, descriptionZh, descriptionEn, sort, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 删除品牌
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> deleteBrand(@PathVariable Long id) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        brandService.deleteBrand(id, merchantId);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}

