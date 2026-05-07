package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.CreateBillRequest;
import com.jm.vote.dto.MerchantBillDTO;
import com.jm.vote.service.BillService;
import com.jm.vote.util.SecurityUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/merchant/bills")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MERCHANT')")
public class BillController {

    private final BillService billService;
    private final SecurityUtil securityUtil;

    @GetMapping
    public ResponseEntity<Page<MerchantBillDTO>> getBills(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(billService.getBills(merchantId, page, size, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MerchantBillDTO> getBill(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBill(id));
    }

    @PostMapping("/{id}/pay")
    public ResponseEntity<MerchantBillDTO> payBill(@PathVariable Long id) {
        return ResponseEntity.ok(billService.payBill(id));
    }
}

