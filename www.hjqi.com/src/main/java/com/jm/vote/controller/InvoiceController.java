package com.jm.vote.controller;

import com.jm.vote.annotation.RequirePayment;
import com.jm.vote.entity.MerchantInvoice;
import com.jm.vote.service.InvoiceService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

/**
 * 发票管理接口
 */
@RestController
@RequestMapping("/api/merchant/invoices")
@RequiredArgsConstructor
@RequirePayment // 发票功能需要缴费后才能使用
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final SecurityUtil securityUtil;
    
    @org.springframework.beans.factory.annotation.Value("${file.upload.path:upload}")
    private String uploadPath;

    /**
     * 获取商家发票列表
     */
    @GetMapping
    public ResponseEntity<List<MerchantInvoice>> getMerchantInvoices() {
        Long merchantId = securityUtil.requireMerchant();
        List<MerchantInvoice> invoices = invoiceService.getMerchantInvoices(merchantId);
        return ResponseEntity.ok(invoices);
    }

    /**
     * 获取发票详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<MerchantInvoice> getInvoiceDetail(@PathVariable Long id) {
        Long merchantId = securityUtil.requireMerchant();
        MerchantInvoice invoice = invoiceService.getInvoiceDetail(id);
        
        // 验证发票属于当前商家
        if (!invoice.getMerchantId().equals(merchantId)) {
            return ResponseEntity.status(403).build();
        }
        
        return ResponseEntity.ok(invoice);
    }

    /**
     * 重新发送发票邮件
     */
    @PostMapping("/{id}/resend")
    public ResponseEntity<Map<String, Object>> resendInvoiceEmail(@PathVariable Long id) {
        Long merchantId = securityUtil.requireMerchant();
        MerchantInvoice invoice = invoiceService.getInvoiceDetail(id);
        
        // 验证发票属于当前商家
        if (!invoice.getMerchantId().equals(merchantId)) {
            return ResponseEntity.status(403).build();
        }
        
        invoiceService.resendInvoiceEmail(id);
        return ResponseEntity.ok(Map.of("success", true, "message", "发票邮件已重新发送"));
    }

    /**
     * 下载发票PDF
     */
    @GetMapping("/{id}/pdf")
    public ResponseEntity<Resource> downloadInvoicePdf(@PathVariable Long id) {
        Long merchantId = securityUtil.requireMerchant();
        MerchantInvoice invoice = invoiceService.getInvoiceDetail(id);
        
        // 验证发票属于当前商家
        if (!invoice.getMerchantId().equals(merchantId)) {
            return ResponseEntity.status(403).build();
        }
        
        if (invoice.getPdfUrl() == null || invoice.getPdfUrl().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            // 从URL中提取文件路径
            String filePath = invoice.getPdfUrl();
            if (filePath.startsWith("/upload/")) {
                filePath = filePath.substring("/upload/".length());
            }
            
            Path pdfPath = Paths.get(uploadPath, filePath);
            File pdfFile = pdfPath.toFile();
            
            if (!pdfFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(pdfFile);
            
            // 构建中文文件名并进行URL编码
            String fileName = "发票_" + invoice.getInvoiceNo() + ".pdf";
            String encodedFileName = java.net.URLEncoder.encode(fileName, "UTF-8").replaceAll("\\+", "%20");
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                            "attachment; filename*=UTF-8''" + encodedFileName)
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * 预览发票PDF（在线查看）
     */
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> previewInvoicePdf(@PathVariable Long id) {
        Long merchantId = securityUtil.requireMerchant();
        MerchantInvoice invoice = invoiceService.getInvoiceDetail(id);
        
        // 验证发票属于当前商家
        if (!invoice.getMerchantId().equals(merchantId)) {
            return ResponseEntity.status(403).build();
        }
        
        if (invoice.getPdfUrl() == null || invoice.getPdfUrl().isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            // 从URL中提取文件路径
            String filePath = invoice.getPdfUrl();
            if (filePath.startsWith("/upload/")) {
                filePath = filePath.substring("/upload/".length());
            }
            
            Path pdfPath = Paths.get(uploadPath, filePath);
            File pdfFile = pdfPath.toFile();
            
            if (!pdfFile.exists()) {
                return ResponseEntity.notFound().build();
            }
            
            Resource resource = new FileSystemResource(pdfFile);
            
            // 在线预览，使用inline而不是attachment
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline")
                    .contentType(MediaType.APPLICATION_PDF)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}

