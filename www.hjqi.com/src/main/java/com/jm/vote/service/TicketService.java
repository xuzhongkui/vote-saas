package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.entity.MerchantTicket;
import com.jm.vote.repository.MerchantTicketMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final MerchantTicketMapper ticketMapper;

    /**
     * 创建工单
     */
    @Transactional
    public MerchantTicket createTicket(Long merchantId, Long userId, String category, String priority, String title, String content) {
        MerchantTicket ticket = MerchantTicket.builder()
                .ticketNo(generateTicketNo())
                .merchantId(merchantId)
                .userId(userId)
                .category(category)
                .priority(priority != null ? priority : "NORMAL")
                .status("OPEN")
                .title(title)
                .content(content)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        ticketMapper.insert(ticket);
        return ticket;
    }

    /**
     * 获取工单列表（商家端 - 仅查看自己的工单）
     */
    public Page<MerchantTicket> getTickets(Long merchantId, String status, String category, int page, int size) {
        LambdaQueryWrapper<MerchantTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantTicket::getMerchantId, merchantId)
                .eq(MerchantTicket::getDeleted, 0);
        
        if (status != null && !status.isEmpty()) {
            wrapper.eq(MerchantTicket::getStatus, status);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(MerchantTicket::getCategory, category);
        }
        
        wrapper.orderByDesc(MerchantTicket::getCreatedAt);
        return ticketMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取工单列表（管理员端 - 查看所有工单）
     */
    public Page<MerchantTicket> getAdminTickets(Long merchantId, String status, String category, int page, int size) {
        LambdaQueryWrapper<MerchantTicket> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantTicket::getDeleted, 0);
        
        if (merchantId != null) {
            wrapper.eq(MerchantTicket::getMerchantId, merchantId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(MerchantTicket::getStatus, status);
        }
        if (category != null && !category.isEmpty()) {
            wrapper.eq(MerchantTicket::getCategory, category);
        }
        
        wrapper.orderByDesc(MerchantTicket::getCreatedAt);
        return ticketMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 管理员处理工单
     */
    @Transactional
    public void handleTicket(Long ticketId, Long adminId, String adminReply, String status) {
        MerchantTicket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("工单不存在");
        }
        
        ticket.setAdminId(adminId);
        ticket.setAdminReply(adminReply);
        ticket.setStatus(status);
        
        if ("RESOLVED".equals(status)) {
            ticket.setResolvedAt(LocalDateTime.now());
        } else if ("CLOSED".equals(status)) {
            ticket.setClosedAt(LocalDateTime.now());
        }
        
        ticket.setUpdatedAt(LocalDateTime.now());
        ticketMapper.updateById(ticket);
    }

    /**
     * 生成工单号
     */
    private String generateTicketNo() {
        return "TK" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

