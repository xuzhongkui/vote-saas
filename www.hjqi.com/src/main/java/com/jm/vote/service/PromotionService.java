package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.PromotionRecordDTO;
import com.jm.vote.dto.PromotionStatisticsDTO;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.PromotionRecord;
import com.jm.vote.entity.PromotionWithdrawal;
import com.jm.vote.entity.User;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.PromotionRecordMapper;
import com.jm.vote.repository.PromotionWithdrawalMapper;
import com.jm.vote.repository.UserMapper;
import com.jm.vote.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PromotionService {

    private final PromotionRecordMapper promotionRecordMapper;
    private final PromotionWithdrawalMapper withdrawalMapper;
    private final MerchantMapper merchantMapper;
    private final UserMapper userMapper;
    private final SecurityUtil securityUtil;
    private final SystemConfigService systemConfigService;
    
    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    /**
     * 生成推广链接
     */
    public String generatePromotionLink(Long merchantId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        // 如果商家没有推广码，生成一个
        if (merchant.getPromotionCode() == null || merchant.getPromotionCode().isEmpty()) {
            String promotionCode = "M" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            merchant.setPromotionCode(promotionCode);
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantMapper.updateById(merchant);
        }
        
        // 始终使用当前的 baseUrl 生成链接，确保使用最新的前端地址
        String currentLink = baseUrl + "/merchant/register?referrer=" + merchant.getPromotionCode();
        
        // 如果数据库中的链接与当前生成的链接不同，更新数据库
        if (merchant.getPromotionLink() == null || !merchant.getPromotionLink().equals(currentLink)) {
            merchant.setPromotionLink(currentLink);
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantMapper.updateById(merchant);
        }
        
        return currentLink;
    }

    /**
     * 记录推广关系（商家邀请商家）- 通过推广码
     */
    @Transactional
    public void recordMerchantPromotionByCode(String promotionCode, Long inviteeMerchantId) {
        // 查找推广人
        Merchant inviter = merchantMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getPromotionCode, promotionCode)
                        .eq(Merchant::getDeleted, 0)
        );
        
        if (inviter == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "推广码无效");
        }
        
        // 检查是否已经记录过
        Long count = promotionRecordMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getInviterId, inviter.getId())
                        .eq(PromotionRecord::getInviteeId, inviteeMerchantId)
                        .eq(PromotionRecord::getInviteeType, "MERCHANT")
        );
        
        if (count != null && count > 0) {
            return; // 已经记录过，不重复记录
        }
        
        // 获取奖励金额配置
        BigDecimal rewardAmount = systemConfigService.getMerchantPromotionReward();
        
        PromotionRecord record = new PromotionRecord();
        record.setMerchantId(inviter.getId());
        record.setInviterId(inviter.getId());
        record.setInviteeId(inviteeMerchantId);
        record.setInviteeType("MERCHANT");
        record.setPromotionCode(promotionCode);
        record.setRewardAmount((int)(rewardAmount.doubleValue() * 100)); // 转换为分
        record.setRewardType("FIXED");
        record.setStatus(0); // 待确认
        record.setCreatedAt(LocalDateTime.now());
        record.setRemark("商家推广奖励");
        promotionRecordMapper.insert(record);
    }

    /**
     * 记录推广关系（商家邀请商家）
     */
    @Transactional
    public void recordMerchantPromotion(Long inviterMerchantId, Long inviteeMerchantId) {
        Merchant inviter = merchantMapper.selectById(inviterMerchantId);
        if (inviter == null || inviter.getPromotionCode() == null) {
            return;
        }
        
        PromotionRecord record = new PromotionRecord();
        record.setMerchantId(inviterMerchantId);
        record.setInviterId(inviterMerchantId);
        record.setInviteeId(inviteeMerchantId);
        record.setInviteeType("MERCHANT");
        record.setPromotionCode(inviter.getPromotionCode());
        record.setRewardAmount(0); // 可以配置奖励金额
        record.setStatus(0); // 待确认
        record.setCreatedAt(LocalDateTime.now());
        promotionRecordMapper.insert(record);
    }

    /**
     * 记录推广关系（商家邀请用户）
     */
    @Transactional
    public void recordUserPromotion(Long merchantId, Long userId) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null || merchant.getPromotionCode() == null) {
            return;
        }
        
        PromotionRecord record = new PromotionRecord();
        record.setMerchantId(merchantId);
        record.setInviterId(merchantId);
        record.setInviteeId(userId);
        record.setInviteeType("USER");
        record.setPromotionCode(merchant.getPromotionCode());
        record.setRewardAmount(0); // 可以配置奖励金额
        record.setStatus(0); // 待确认
        record.setCreatedAt(LocalDateTime.now());
        promotionRecordMapper.insert(record);
    }

    /**
     * 获取推广统计
     */
    public PromotionStatisticsDTO getPromotionStatistics(Long merchantId) {
        PromotionStatisticsDTO stats = new PromotionStatisticsDTO();
        
        // 推广商家数（只统计已完成缴费、已确认奖励的商家）
        Long merchantCount = promotionRecordMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getMerchantId, merchantId)
                        .eq(PromotionRecord::getInviteeType, "MERCHANT")
                        .eq(PromotionRecord::getStatus, 1) // 仅统计已确认的推广记录（代表已缴费）
        );
        stats.setPromotedMerchantCount(merchantCount);
        
        // 推广用户数
        Long userCount = promotionRecordMapper.selectCount(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getMerchantId, merchantId)
                        .eq(PromotionRecord::getInviteeType, "USER")
        );
        stats.setPromotedUserCount(userCount);
        
        // 奖励统计
        java.util.List<PromotionRecord> records = promotionRecordMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getMerchantId, merchantId)
        );
        
        long totalReward = 0;
        long confirmedReward = 0;
        long pendingReward = 0;
        
        for (PromotionRecord record : records) {
            totalReward += record.getRewardAmount() != null ? record.getRewardAmount() : 0;
            if (record.getStatus() != null && record.getStatus() == 1) {
                confirmedReward += record.getRewardAmount() != null ? record.getRewardAmount() : 0;
            } else if (record.getStatus() != null && record.getStatus() == 0) {
                pendingReward += record.getRewardAmount() != null ? record.getRewardAmount() : 0;
            }
        }
        
        stats.setTotalRewardAmount(totalReward);
        stats.setConfirmedRewardAmount(confirmedReward);
        stats.setPendingRewardAmount(pendingReward);
        
        return stats;
    }
    
    /**
     * 确认推广奖励（当被邀请商家完成缴费后调用）
     */
    @Transactional
    public void confirmMerchantPromotionReward(Long inviteeMerchantId) {
        // 查找推广记录
        PromotionRecord record = promotionRecordMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getInviteeId, inviteeMerchantId)
                        .eq(PromotionRecord::getInviteeType, "MERCHANT")
                        .eq(PromotionRecord::getStatus, 0)
        );
        
        if (record == null) {
            log.info("未找到待确认的推广记录: inviteeMerchantId={}", inviteeMerchantId);
            return;
        }
        
        // 更新推广记录状态
        record.setStatus(1); // 已确认
        record.setConfirmedAt(LocalDateTime.now());
        record.setSettlementTime(LocalDateTime.now());
        promotionRecordMapper.updateById(record);
        
        // 更新邀请人的奖励金额
        Merchant inviter = merchantMapper.selectById(record.getInviterId());
        if (inviter != null) {
            BigDecimal rewardAmount = BigDecimal.valueOf(record.getRewardAmount()).divide(BigDecimal.valueOf(100));
            inviter.setTotalReward(inviter.getTotalReward().add(rewardAmount));
            inviter.setAvailableReward(inviter.getAvailableReward().add(rewardAmount));
            inviter.setUpdatedAt(LocalDateTime.now());
            merchantMapper.updateById(inviter);
            
            log.info("推广奖励已确认: inviterId={}, inviteeId={}, reward={}", 
                    record.getInviterId(), inviteeMerchantId, rewardAmount);
        }
    }
    
    /**
     * 记录用户消费推广奖励
     */
    @Transactional
    public void recordUserConsumptionReward(Long userId, Long orderId, BigDecimal orderAmount) {
        // 查找用户的推广关系
        PromotionRecord promotionRecord = promotionRecordMapper.selectOne(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getInviteeId, userId)
                        .eq(PromotionRecord::getInviteeType, "USER")
                        .eq(PromotionRecord::getStatus, 1) // 已确认的推广关系
        );
        
        if (promotionRecord == null) {
            return; // 没有推广关系
        }
        
        // 获取用户推广奖励比例
        BigDecimal rewardRate = systemConfigService.getUserPromotionRewardRate();
        BigDecimal rewardAmount = orderAmount.multiply(rewardRate).divide(BigDecimal.valueOf(100));
        
        // 创建新的奖励记录
        PromotionRecord rewardRecord = new PromotionRecord();
        rewardRecord.setMerchantId(promotionRecord.getMerchantId());
        rewardRecord.setInviterId(promotionRecord.getInviterId());
        rewardRecord.setInviteeId(userId);
        rewardRecord.setInviteeType("USER");
        rewardRecord.setPromotionCode(promotionRecord.getPromotionCode());
        rewardRecord.setRewardAmount((int)(rewardAmount.doubleValue() * 100)); // 转换为分
        rewardRecord.setRewardType("PERCENTAGE");
        rewardRecord.setRewardRate(rewardRate);
        rewardRecord.setOrderAmount(orderAmount);
        rewardRecord.setStatus(1); // 直接确认
        rewardRecord.setCreatedAt(LocalDateTime.now());
        rewardRecord.setConfirmedAt(LocalDateTime.now());
        rewardRecord.setSettlementTime(LocalDateTime.now());
        rewardRecord.setRemark("用户消费推广奖励 - 订单号: " + orderId);
        promotionRecordMapper.insert(rewardRecord);
        
        // 更新邀请人的奖励金额
        Merchant inviter = merchantMapper.selectById(promotionRecord.getInviterId());
        if (inviter != null) {
            inviter.setTotalReward(inviter.getTotalReward().add(rewardAmount));
            inviter.setAvailableReward(inviter.getAvailableReward().add(rewardAmount));
            inviter.setUpdatedAt(LocalDateTime.now());
            merchantMapper.updateById(inviter);
            
            log.info("用户消费推广奖励已记录: inviterId={}, userId={}, orderId={}, reward={}", 
                    promotionRecord.getInviterId(), userId, orderId, rewardAmount);
        }
    }
    
    /**
     * 申请提现
     */
    @Transactional
    public Map<String, Object> applyWithdrawal(Long merchantId, BigDecimal amount, 
                                               String accountType, String accountName, String accountNumber) {
        Merchant merchant = merchantMapper.selectById(merchantId);
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        // 验证提现金额
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "提现金额必须大于0");
        }
        
        if (amount.compareTo(merchant.getAvailableReward()) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "提现金额超过可用余额");
        }
        
        // 计算手续费（可配置）
        BigDecimal feeRate = BigDecimal.valueOf(0.01); // 1%手续费
        BigDecimal fee = amount.multiply(feeRate).setScale(2, RoundingMode.HALF_UP);
        BigDecimal actualAmount = amount.subtract(fee);
        
        // 生成提现单号
        String withdrawalNo = "WD" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) 
                + String.format("%04d", (int)(Math.random() * 10000));
        
        // 创建提现记录
        PromotionWithdrawal withdrawal = new PromotionWithdrawal();
        withdrawal.setMerchantId(merchantId);
        withdrawal.setWithdrawalNo(withdrawalNo);
        withdrawal.setAmount(amount);
        withdrawal.setFee(fee);
        withdrawal.setActualAmount(actualAmount);
        withdrawal.setAccountType(accountType);
        withdrawal.setAccountName(accountName);
        withdrawal.setAccountNumber(accountNumber);
        withdrawal.setStatus(0); // 待审核
        withdrawal.setCreatedAt(LocalDateTime.now());
        withdrawal.setUpdatedAt(LocalDateTime.now());
        withdrawalMapper.insert(withdrawal);
        
        // 冻结可用余额
        merchant.setAvailableReward(merchant.getAvailableReward().subtract(amount));
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        
        log.info("提现申请已创建: merchantId={}, withdrawalNo={}, amount={}", merchantId, withdrawalNo, amount);
        
        Map<String, Object> result = new HashMap<>();
        result.put("withdrawalNo", withdrawalNo);
        result.put("amount", amount);
        result.put("fee", fee);
        result.put("actualAmount", actualAmount);
        result.put("status", "待审核");
        result.put("message", "提现申请已提交，请等待审核");
        
        return result;
    }
    
    /**
     * 获取提现记录列表
     */
    public List<PromotionWithdrawal> getWithdrawalRecords(Long merchantId) {
        return withdrawalMapper.selectList(
                new LambdaQueryWrapper<PromotionWithdrawal>()
                        .eq(PromotionWithdrawal::getMerchantId, merchantId)
                        .orderByDesc(PromotionWithdrawal::getCreatedAt)
        );
    }
    
    /**
     * 获取推广记录列表（分页）
     */
    public Page<PromotionRecordDTO> getPromotionRecords(Long merchantId, int page, int size) {
        Page<PromotionRecord> pageParam = new Page<>(page, size);
        Page<PromotionRecord> recordPage = promotionRecordMapper.selectPage(pageParam,
                new LambdaQueryWrapper<PromotionRecord>()
                        .eq(PromotionRecord::getMerchantId, merchantId)
                        .orderByDesc(PromotionRecord::getCreatedAt)
        );
        
        // 转换为DTO并填充被邀请人名称
        Page<PromotionRecordDTO> dtoPage = new Page<>(recordPage.getCurrent(), recordPage.getSize(), recordPage.getTotal());
        List<PromotionRecordDTO> dtoList = recordPage.getRecords().stream().map(record -> {
            PromotionRecordDTO dto = new PromotionRecordDTO();
            dto.setId(record.getId());
            dto.setMerchantId(record.getMerchantId());
            dto.setInviterId(record.getInviterId());
            dto.setInviteeId(record.getInviteeId());
            dto.setInviteeType(record.getInviteeType());
            dto.setPromotionCode(record.getPromotionCode());
            dto.setRewardAmount(record.getRewardAmount());
            dto.setRewardType(record.getRewardType());
            dto.setRewardRate(record.getRewardRate());
            dto.setOrderAmount(record.getOrderAmount());
            dto.setStatus(record.getStatus());
            dto.setCreatedAt(record.getCreatedAt());
            dto.setConfirmedAt(record.getConfirmedAt());
            dto.setSettlementTime(record.getSettlementTime());
            dto.setRemark(record.getRemark());
            
            // 根据被邀请人类型查询名称
            String inviteeName = null;
            if (record.getInviteeId() != null && record.getInviteeType() != null) {
                if ("MERCHANT".equals(record.getInviteeType())) {
                    Merchant merchant = merchantMapper.selectById(record.getInviteeId());
                    if (merchant != null) {
                        // 优先使用店铺中文名称，如果没有则使用英文名称，都没有则使用用户名
                        if (merchant.getShopNameZh() != null && !merchant.getShopNameZh().trim().isEmpty()) {
                            inviteeName = merchant.getShopNameZh();
                        } else if (merchant.getShopNameEn() != null && !merchant.getShopNameEn().trim().isEmpty()) {
                            inviteeName = merchant.getShopNameEn();
                        } else {
                            inviteeName = merchant.getUsername();
                        }
                    }
                } else if ("USER".equals(record.getInviteeType())) {
                    User user = userMapper.selectById(record.getInviteeId());
                    if (user != null) {
                        inviteeName = user.getUsername();
                    }
                }
            }
            dto.setInviteeName(inviteeName);
            
            return dto;
        }).collect(java.util.stream.Collectors.toList());
        
        dtoPage.setRecords(dtoList);
        return dtoPage;
    }
    
    /**
     * 验证推广码
     */
    public Map<String, Object> verifyPromotionCode(String promotionCode) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getPromotionCode, promotionCode)
                        .eq(Merchant::getDeleted, 0)
        );
        
        Map<String, Object> result = new HashMap<>();
        if (merchant != null) {
            result.put("valid", true);
            // 优先使用店铺中文名，如果没有则使用英文名，都不存在则返回空（前端会处理）
            String shopName = null;
            if (merchant.getShopNameZh() != null && !merchant.getShopNameZh().trim().isEmpty()) {
                shopName = merchant.getShopNameZh();
            } else if (merchant.getShopNameEn() != null && !merchant.getShopNameEn().trim().isEmpty()) {
                shopName = merchant.getShopNameEn();
            }
            result.put("shopName", shopName);
            result.put("message", "推广码有效");
        } else {
            result.put("valid", false);
            result.put("message", "推广码无效");
        }

        return result;
    }
    
    /**
     * 审核提现申请（管理员）
     */
    @Transactional
    public Map<String, Object> auditWithdrawal(Long withdrawalId, Long auditorId, boolean approved, String remark) {
        PromotionWithdrawal withdrawal = withdrawalMapper.selectById(withdrawalId);
        if (withdrawal == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "提现记录不存在");
        }
        
        if (withdrawal.getStatus() != 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "提现记录状态不是待审核");
        }
        
        Merchant merchant = merchantMapper.selectById(withdrawal.getMerchantId());
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        
        if (approved) {
            // 审核通过
            withdrawal.setStatus(1); // 审核通过
            withdrawal.setAuditRemark(remark != null ? remark : "审核通过");
        } else {
            // 审核拒绝，退回金额
            withdrawal.setStatus(3); // 已拒绝
            withdrawal.setAuditRemark(remark != null ? remark : "审核未通过");
            
            // 退回可用余额
            merchant.setAvailableReward(merchant.getAvailableReward().add(withdrawal.getAmount()));
            merchant.setUpdatedAt(LocalDateTime.now());
            merchantMapper.updateById(merchant);
        }
        
        withdrawal.setAuditorId(auditorId);
        withdrawal.setAuditTime(LocalDateTime.now());
        withdrawal.setUpdatedAt(LocalDateTime.now());
        withdrawalMapper.updateById(withdrawal);
        
        log.info("提现审核完成: withdrawalId={}, approved={}, auditorId={}", withdrawalId, approved, auditorId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("withdrawalId", withdrawalId);
        result.put("status", withdrawal.getStatus());
        result.put("message", approved ? "审核通过" : "审核拒绝");
        
        return result;
    }
    
    /**
     * 确认打款（管理员）
     */
    @Transactional
    public Map<String, Object> confirmTransfer(Long withdrawalId, String transferVoucher) {
        PromotionWithdrawal withdrawal = withdrawalMapper.selectById(withdrawalId);
        if (withdrawal == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "提现记录不存在");
        }
        
        if (withdrawal.getStatus() != 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "提现记录状态不是审核通过");
        }
        
        withdrawal.setStatus(2); // 已打款
        withdrawal.setTransferTime(LocalDateTime.now());
        withdrawal.setTransferVoucher(transferVoucher);
        withdrawal.setUpdatedAt(LocalDateTime.now());
        withdrawalMapper.updateById(withdrawal);
        
        log.info("提现打款完成: withdrawalId={}", withdrawalId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("withdrawalId", withdrawalId);
        result.put("status", withdrawal.getStatus());
        result.put("message", "打款完成");
        
        return result;
    }
}

