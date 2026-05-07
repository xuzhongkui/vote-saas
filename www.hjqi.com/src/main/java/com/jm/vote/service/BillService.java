package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.CreateBillRequest;
import com.jm.vote.dto.MerchantBillDTO;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.MerchantBill;
import com.jm.vote.entity.MerchantInvoice;
import com.jm.vote.repository.MerchantBillMapper;
import com.jm.vote.repository.MerchantMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillService {

    private final MerchantBillMapper billMapper;
    private final MerchantMapper merchantMapper;
    private final InvoiceService invoiceService;
    private final SystemConfigService systemConfigService;

    public Page<MerchantBillDTO> getBills(Long merchantId, int page, int size, Integer status) {
        LambdaQueryWrapper<MerchantBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantBill::getMerchantId, merchantId)
                .eq(MerchantBill::getDeleted, 0);
        if (status != null) {
            wrapper.eq(MerchantBill::getStatus, status);
        }
        wrapper.orderByDesc(MerchantBill::getCreatedAt);
        
        Page<MerchantBill> billPage = new Page<>(page, size);
        Page<MerchantBill> result = billMapper.selectPage(billPage, wrapper);
        Page<MerchantBillDTO> dtoPage = new Page<>(page, size, result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toBillDTO).collect(java.util.stream.Collectors.toList()));
        return dtoPage;
    }

    // 管理员查询所有账单（可选按商家ID和状态筛选）
    public Page<MerchantBillDTO> getAdminBills(Long merchantId, int page, int size, Integer status) {
        LambdaQueryWrapper<MerchantBill> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantBill::getDeleted, 0);
        
        // 如果指定了商家ID，则按商家ID筛选
        if (merchantId != null && merchantId > 0) {
            wrapper.eq(MerchantBill::getMerchantId, merchantId);
        }
        
        // 如果指定了状态，则按状态筛选
        if (status != null) {
            wrapper.eq(MerchantBill::getStatus, status);
        }
        
        wrapper.orderByDesc(MerchantBill::getCreatedAt);
        
        Page<MerchantBill> billPage = new Page<>(page, size);
        Page<MerchantBill> result = billMapper.selectPage(billPage, wrapper);
        Page<MerchantBillDTO> dtoPage = new Page<>(page, size, result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toBillDTO).collect(java.util.stream.Collectors.toList()));
        return dtoPage;
    }

    public MerchantBillDTO getBill(Long id) {
        MerchantBill bill = billMapper.selectById(id);
        if (bill == null || bill.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "账单不存在");
        }
        return toBillDTO(bill);
    }

    @Transactional
    public MerchantBillDTO createBill(CreateBillRequest request) {
        MerchantBill bill = new MerchantBill();
        bill.setMerchantId(request.getMerchantId());
        bill.setBillNo(generateBillNo());
        bill.setBillType(request.getBillType());
        bill.setAmount(request.getAmount());
        bill.setStatus(0); // 待支付
        bill.setStartDate(request.getStartDate());
        bill.setEndDate(request.getEndDate());
        bill.setDeleted(0);
        bill.setCreatedAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        billMapper.insert(bill);
        return toBillDTO(bill);
    }

    @Transactional
    public MerchantBillDTO payBill(Long id) {
        MerchantBill bill = billMapper.selectById(id);
        if (bill == null || bill.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "账单不存在");
        }
        if (bill.getStatus() != null && bill.getStatus() == 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "账单已支付");
        }
        
        bill.setStatus(1); // 已支付
        bill.setPaidAt(LocalDateTime.now());
        bill.setUpdatedAt(LocalDateTime.now());
        billMapper.updateById(bill);
        
        // 自动生成发票（如果配置开启）
        if (systemConfigService.isInvoiceAutoGenerate()) {
            try {
                MerchantInvoice invoice = invoiceService.generateBillInvoice(bill);
                if (invoice != null && invoice.getPdfUrl() != null) {
                    // 将发票URL关联到账单
                    bill.setInvoiceUrl(invoice.getPdfUrl());
                    bill.setUpdatedAt(LocalDateTime.now());
                    billMapper.updateById(bill);
                    log.info("账单支付后自动生成发票成功: billId={}, invoiceNo={}", id, invoice.getInvoiceNo());
                }
            } catch (Exception e) {
                log.error("账单支付后自动生成发票失败: billId={}, error={}", id, e.getMessage(), e);
                // 发票生成失败不影响支付流程
            }
        }
        
        return toBillDTO(bill);
    }

    @Transactional
    public MerchantBillDTO generateInvoice(Long id, String invoiceUrl) {
        MerchantBill bill = billMapper.selectById(id);
        if (bill == null || bill.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "账单不存在");
        }
        
        bill.setInvoiceUrl(invoiceUrl);
        bill.setUpdatedAt(LocalDateTime.now());
        billMapper.updateById(bill);
        
        return toBillDTO(bill);
    }

    private String generateBillNo() {
        return "BILL" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
    }

    private MerchantBillDTO toBillDTO(MerchantBill bill) {
        MerchantBillDTO dto = new MerchantBillDTO();
        dto.setId(bill.getId());
        dto.setMerchantId(bill.getMerchantId());
        dto.setBillNo(bill.getBillNo());
        dto.setBillType(bill.getBillType());
        dto.setAmount(bill.getAmount());
        dto.setStatus(bill.getStatus());
        dto.setStartDate(bill.getStartDate());
        dto.setEndDate(bill.getEndDate());
        dto.setInvoiceUrl(bill.getInvoiceUrl());
        dto.setPaidAt(bill.getPaidAt());
        dto.setCreatedAt(bill.getCreatedAt());
        dto.setUpdatedAt(bill.getUpdatedAt());
        
        // 查询商家名称
        if (bill.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectById(bill.getMerchantId());
            if (merchant != null) {
                dto.setMerchantName(merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getUsername());
            }
        }
        
        return dto;
    }
}

