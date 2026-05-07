package com.jm.vote.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.entity.MerchantTicket;
import com.jm.vote.service.TicketService;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/ticket")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final SecurityUtil securityUtil;

    /**
     * 创建工单（仅商家可用）
     */
    @PostMapping("/create")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Map<String, Object>> createTicket(
            @RequestParam String category,
            @RequestParam(required = false, defaultValue = "NORMAL") String priority,
            @RequestParam String title,
            @RequestParam String content) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "商家ID不能为空"));
        }
        
        MerchantTicket ticket = ticketService.createTicket(merchantId, null, category, priority, title, content);
        
        Map<String, Object> response = new HashMap<>();
        response.put("ticketId", ticket.getId());
        response.put("ticketNo", ticket.getTicketNo());
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    /**
     * 获取工单列表（商家端 - 仅查看自己的工单）
     */
    @GetMapping("/merchant/list")
    @PreAuthorize("hasRole('MERCHANT')")
    public ResponseEntity<Page<MerchantTicket>> getMerchantTickets(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Long merchantId = securityUtil.getCurrentMerchantId();
        if (merchantId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        Page<MerchantTicket> tickets = ticketService.getTickets(merchantId, status, category, page, size);
        return ResponseEntity.ok(tickets);
    }

    /**
     * 获取工单列表（管理员端 - 查看所有工单）
     */
    @GetMapping("/admin/list")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Page<MerchantTicket>> getAdminTickets(
            @RequestParam(required = false) Long merchantId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Page<MerchantTicket> tickets = ticketService.getAdminTickets(merchantId, status, category, page, size);
        return ResponseEntity.ok(tickets);
    }

    /**
     * 管理员处理工单
     */
    @PostMapping("/handle")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<Map<String, Object>> handleTicket(
            @RequestParam Long ticketId,
            @RequestParam String adminReply,
            @RequestParam String status) {
        
        Long adminId = securityUtil.getCurrentAdminId();
        if (adminId == null) {
            return ResponseEntity.badRequest().build();
        }
        
        ticketService.handleTicket(ticketId, adminId, adminReply, status);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}

