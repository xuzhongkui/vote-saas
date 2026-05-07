package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.annotation.RequirePayment;
import com.jm.vote.dto.*;
import com.jm.vote.service.MerchantMallService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/merchant")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
@RequirePayment // 默认所有接口都需要缴费，个别接口可以单独排除
public class MerchantMallController {

    private final MerchantMallService merchantMallService;

    // ==================== 分类管理 ====================

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getCategories() {
        return ResponseEntity.ok(merchantMallService.getCategories());
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryDTO> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantMallService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable Long id, @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(merchantMallService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Map<String, String>> deleteCategory(@PathVariable Long id) {
        merchantMallService.deleteCategory(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // ==================== 商品管理 ====================

    @GetMapping("/products")
    public ResponseEntity<Page<ProductListItemDTO>> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status) {
        return ResponseEntity.ok(merchantMallService.getProducts(page, size, categoryId, status));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDetailDTO> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(merchantMallService.getProduct(id));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductDetailDTO> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantMallService.createProduct(request));
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<ProductDetailDTO> updateProduct(@PathVariable Long id, @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(merchantMallService.updateProduct(id, request));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        merchantMallService.deleteProduct(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // ==================== SKU管理 ====================

    @GetMapping("/products/{productId}/skus")
    public ResponseEntity<List<ProductSkuDTO>> getProductSkus(@PathVariable Long productId) {
        return ResponseEntity.ok(merchantMallService.getProductSkus(productId));
    }

    @PostMapping("/products/skus")
    public ResponseEntity<ProductSkuDTO> createProductSku(@Valid @RequestBody CreateProductSkuRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(merchantMallService.createProductSku(request));
    }

    @PutMapping("/products/skus/{id}")
    public ResponseEntity<ProductSkuDTO> updateProductSku(@PathVariable Long id, @RequestBody UpdateProductSkuRequest request) {
        return ResponseEntity.ok(merchantMallService.updateProductSku(id, request));
    }

    @DeleteMapping("/products/skus/{id}")
    public ResponseEntity<Map<String, String>> deleteProductSku(@PathVariable Long id) {
        merchantMallService.deleteProductSku(id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // ==================== 订单管理 ====================

    @GetMapping("/orders")
    public ResponseEntity<Page<MerchantOrderListDTO>> getOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(merchantMallService.getOrders(page, size, status));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDetailDTO> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(merchantMallService.getOrder(id));
    }

    @PostMapping("/orders/{id}/confirm")
    public ResponseEntity<OrderDetailDTO> confirmOrder(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(merchantMallService.confirmOrder(id, request));
    }

    @PostMapping("/orders/{id}/ship")
    public ResponseEntity<OrderDetailDTO> shipOrder(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(merchantMallService.shipOrder(id, request));
    }

    @PostMapping("/orders/{id}/complete")
    public ResponseEntity<OrderDetailDTO> completeOrder(@PathVariable Long id) {
        return ResponseEntity.ok(merchantMallService.completeOrder(id));
    }

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderDetailDTO> cancelOrder(@PathVariable Long id, @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(merchantMallService.cancelOrder(id, request));
    }

    @PutMapping("/orders/{id}/remark")
    public ResponseEntity<OrderDetailDTO> updateOrderRemark(
            @PathVariable Long id,
            @RequestBody UpdateOrderStatusRequest request) {
        return ResponseEntity.ok(merchantMallService.updateOrderRemark(id, request));
    }

    @DeleteMapping("/orders/{id}")
    public ResponseEntity<Map<String, String>> deleteOrder(@PathVariable Long id) {
        merchantMallService.deleteOrder(id);
        return ResponseEntity.ok(Map.of("message", "订单已删除"));
    }

    // ==================== 店铺配置管理 ====================

    @GetMapping("/config")
    public ResponseEntity<MerchantConfigDTO> getMerchantConfig() {
        return ResponseEntity.ok(merchantMallService.getMerchantConfig());
    }

    @PutMapping("/config")
    public ResponseEntity<MerchantConfigDTO> updateMerchantConfig(@RequestBody UpdateMerchantConfigRequest request) {
        return ResponseEntity.ok(merchantMallService.updateMerchantConfig(request));
    }

    // ==================== 店铺信息管理 ====================

    @GetMapping("/info")
    // 查看商家信息不需要缴费检查，允许未缴费商家查看自己的信息
    public ResponseEntity<MerchantInfoDTO> getMerchantInfo() {
        return ResponseEntity.ok(merchantMallService.getMerchantInfo());
    }

    @PutMapping("/info")
    @RequirePayment // 修改商家信息需要缴费
    public ResponseEntity<MerchantInfoDTO> updateMerchantInfo(@RequestBody UpdateMerchantInfoRequest request) {
        return ResponseEntity.ok(merchantMallService.updateMerchantInfo(request));
    }

    // ==================== 数据统计 ====================

    @GetMapping("/statistics")
    public ResponseEntity<MerchantStatisticsDTO> getStatistics() {
        return ResponseEntity.ok(merchantMallService.getStatistics());
    }
}

