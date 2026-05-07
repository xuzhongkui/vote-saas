package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.annotation.OperationLog;
import com.jm.vote.dto.CreateBillRequest;
import com.jm.vote.dto.MerchantBillDTO;
import com.jm.vote.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin/bills")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminBillController {

    private final BillService billService;

    @GetMapping
    public ResponseEntity<Page<MerchantBillDTO>> getBills(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) Integer status) {
        // 管理员可以查看所有账单
        return ResponseEntity.ok(billService.getAdminBills(merchantId, page, size, status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MerchantBillDTO> getBill(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBill(id));
    }

    @PostMapping
    @OperationLog(operation = "CREATE", module = "BILL", description = "创建账单")
    public ResponseEntity<MerchantBillDTO> createBill(@Valid @RequestBody CreateBillRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.createBill(request));
    }

    @PostMapping("/{id}/invoice")
    @OperationLog(operation = "CREATE", module = "BILL", description = "生成发票")
    public ResponseEntity<MerchantBillDTO> generateInvoice(
            @PathVariable Long id,
            @RequestParam String invoiceUrl) {
        return ResponseEntity.ok(billService.generateInvoice(id, invoiceUrl));
    }
}

