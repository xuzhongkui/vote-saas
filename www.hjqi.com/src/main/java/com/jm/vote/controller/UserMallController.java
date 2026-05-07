package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.*;
import com.jm.vote.service.UserMallService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserMallController {

    private final UserMallService userMallService;

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> listCategories(HttpServletRequest request) {
        return ResponseEntity.ok(userMallService.listCategories(request));
    }

    @GetMapping("/products")
    public ResponseEntity<Page<ProductListItemDTO>> pageProducts(
            HttpServletRequest request,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userMallService.pageProducts(request, categoryId, keyword, page, size));
    }
    
    @GetMapping("/products/recommend")
    public ResponseEntity<Page<ProductListItemDTO>> getRecommendProducts(
            HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(userMallService.getRecommendProducts(request, page, size));
    }

    @GetMapping("/products/{id}")
    public ResponseEntity<ProductDetailDTO> productDetail(HttpServletRequest request, @PathVariable Long id) {
        return ResponseEntity.ok(userMallService.getProductDetail(request, id));
    }

    @GetMapping("/cart")
    public ResponseEntity<List<CartItemDTO>> listCart(HttpServletRequest request) {
        return ResponseEntity.ok(userMallService.listCartItems(request));
    }

    @PostMapping("/cart")
    public ResponseEntity<?> addToCart(
            HttpServletRequest request,
            @RequestParam Long productId,
            @RequestParam(required = false) Long skuId,
            @RequestParam Integer quantity
    ) {
        userMallService.addOrUpdateCartItem(request, productId, skuId, quantity);
        return ResponseEntity.ok(Map.of("message", "加入购物车成功"));
    }

    @PutMapping("/cart/{id}")
    public ResponseEntity<?> updateCart(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestParam(required = false) Integer quantity,
            @RequestParam(required = false) Boolean selected
    ) {
        userMallService.updateCartItem(request, id, quantity, selected);
        return ResponseEntity.ok(Map.of("message", "更新成功"));
    }

    @DeleteMapping("/cart/{id}")
    public ResponseEntity<?> deleteCart(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        userMallService.deleteCartItem(request, id);
        return ResponseEntity.ok(Map.of("message", "已删除"));
    }

    @PostMapping("/orders")
    public ResponseEntity<OrderDetailDTO> createOrder(
            HttpServletRequest request,
            @RequestBody CreateOrderRequest body
    ) {
        return ResponseEntity.ok(userMallService.createOrder(request, body));
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderSummaryDTO>> listOrders(HttpServletRequest request) {
        return ResponseEntity.ok(userMallService.listUserOrders(request));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<OrderDetailDTO> getOrderDetail(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userMallService.getOrderDetail(request, id));
    }

    // ==================== 地址管理 ====================

    @GetMapping("/addresses")
    public ResponseEntity<List<AddressDTO>> listAddresses(HttpServletRequest request) {
        return ResponseEntity.ok(userMallService.listAddresses(request));
    }

    @GetMapping("/addresses/{id}")
    public ResponseEntity<AddressDTO> getAddress(HttpServletRequest request, @PathVariable Long id) {
        return ResponseEntity.ok(userMallService.getAddress(request, id));
    }

    @PostMapping("/addresses")
    public ResponseEntity<AddressDTO> createAddress(
            HttpServletRequest request,
            @RequestBody CreateAddressRequest req
    ) {
        return ResponseEntity.ok(userMallService.createAddress(request, req));
    }

    @PutMapping("/addresses/{id}")
    public ResponseEntity<AddressDTO> updateAddress(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody UpdateAddressRequest req
    ) {
        return ResponseEntity.ok(userMallService.updateAddress(request, id, req));
    }

    @DeleteMapping("/addresses/{id}")
    public ResponseEntity<Map<String, String>> deleteAddress(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        userMallService.deleteAddress(request, id);
        return ResponseEntity.ok(Map.of("message", "删除成功"));
    }

    // ==================== 个人资料管理 ====================

    @GetMapping("/profile")
    public ResponseEntity<UserInfoDTO> getProfile(HttpServletRequest request) {
        return ResponseEntity.ok(userMallService.getProfile(request));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserInfoDTO> updateProfile(
            HttpServletRequest request,
            @RequestBody UpdateProfileRequest req
    ) {
        return ResponseEntity.ok(userMallService.updateProfile(request, req));
    }

    // ==================== 绑定商家（首次登录）====================

    @PostMapping("/bind-merchant")
    public ResponseEntity<BindMerchantResponse> bindMerchant(
            HttpServletRequest request,
            @RequestBody ChangeMerchantRequest req
    ) {
        return ResponseEntity.ok(userMallService.bindMerchant(request, req));
    }

    // ==================== 换绑商家 ====================

    @PostMapping("/change-merchant")
    public ResponseEntity<BindMerchantResponse> changeMerchant(
            HttpServletRequest request,
            @RequestBody ChangeMerchantRequest req
    ) {
        return ResponseEntity.ok(userMallService.changeMerchant(request, req));
    }

    // ==================== 模拟支付 ====================

    @PostMapping("/orders/{id}/pay/mock")
    public ResponseEntity<OrderDetailDTO> mockPayOrder(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userMallService.mockPayOrder(request, id));
    }

    // ==================== 用户确认收货 ====================

    @PostMapping("/orders/{id}/confirm-receive")
    public ResponseEntity<OrderDetailDTO> confirmReceive(
            HttpServletRequest request,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(userMallService.confirmReceive(request, id));
    }

    // ==================== 用户取消订单 ====================

    @PostMapping("/orders/{id}/cancel")
    public ResponseEntity<OrderDetailDTO> cancelOrder(
            HttpServletRequest request,
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) {
        String cancelReason = body != null ? body.get("cancelReason") : null;
        return ResponseEntity.ok(userMallService.userCancelOrder(request, id, cancelReason));
    }

    // ==================== 获取商家信息（公开接口）====================

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<Map<String, Object>> getMerchantInfo(@PathVariable Long merchantId) {
        return ResponseEntity.ok(userMallService.getMerchantInfo(merchantId));
    }
}


