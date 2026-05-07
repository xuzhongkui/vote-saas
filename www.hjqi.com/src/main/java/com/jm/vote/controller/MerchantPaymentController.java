package com.jm.vote.controller;

import com.jm.vote.service.MerchantPaymentService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant/payment")
@RequiredArgsConstructor
public class MerchantPaymentController {

    private final MerchantPaymentService merchantPaymentService;
    private final SecurityUtil securityUtil;

    /**
     * 创建商家入驻缴费订单（公开接口，用于注册后缴费）
     */
    @PostMapping("/registration/create")
    public ResponseEntity<?> createRegistrationPayment(@RequestParam(required = false) Long merchantId) {
        // 如果未提供merchantId，尝试从当前登录用户获取
        if (merchantId == null) {
            merchantId = securityUtil.getCurrentMerchantId();
        }
        
        if (merchantId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "商家ID不能为空"));
        }
        
        Map<String, Object> result = merchantPaymentService.createRegistrationPaymentOrder(merchantId);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询商家的支付订单（公开接口，用于注册后查询）
     */
    @GetMapping("/order")
    public ResponseEntity<?> getPaymentOrder(@RequestParam(required = false) Long merchantId) {
        // 如果未提供merchantId，尝试从当前登录用户获取
        if (merchantId == null) {
            merchantId = securityUtil.getCurrentMerchantId();
        }
        
        if (merchantId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "商家ID不能为空"));
        }
        
        Map<String, Object> result = merchantPaymentService.getMerchantPaymentOrder(merchantId);
        return ResponseEntity.ok(result);
    }

    /**
     * 查询支付订单状态
     */
    @GetMapping("/status/{paymentNo}")
    public ResponseEntity<?> getPaymentStatus(@PathVariable String paymentNo) {
        Map<String, Object> result = merchantPaymentService.getPaymentOrderStatus(paymentNo);
        return ResponseEntity.ok(result);
    }

    /**
     * 支付回调接口（公开接口，供支付平台回调）
     */
    @PostMapping("/callback")
    public ResponseEntity<?> paymentCallback(@RequestBody Map<String, Object> callbackData) {
        String paymentNo = (String) callbackData.get("paymentNo");
        String thirdPartyOrderNo = (String) callbackData.get("thirdPartyOrderNo");
        String data = callbackData.toString();
        
        merchantPaymentService.handlePaymentCallback(paymentNo, thirdPartyOrderNo, data);
        
        return ResponseEntity.ok(Map.of("success", true, "message", "回调处理成功"));
    }

    /**
     * 模拟支付成功（仅用于测试）
     */
    @PostMapping("/mock-success/{paymentNo}")
    public ResponseEntity<?> mockPaymentSuccess(@PathVariable String paymentNo) {
        String thirdPartyOrderNo = "MOCK_" + System.currentTimeMillis();
        String callbackData = "{\"mock\": true, \"paymentNo\": \"" + paymentNo + "\"}";
        
        merchantPaymentService.handlePaymentCallback(paymentNo, thirdPartyOrderNo, callbackData);
        
        return ResponseEntity.ok(Map.of("success", true, "message", "模拟支付成功"));
    }
}

