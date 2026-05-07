package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.*;
import com.jm.vote.entity.Merchant;
import com.jm.vote.entity.Order;
import com.jm.vote.entity.PromotionWithdrawal;
import com.jm.vote.entity.SystemConfig;
import com.jm.vote.entity.User;
import com.jm.vote.repository.MerchantMapper;
import com.jm.vote.repository.OrderMapper;
import com.jm.vote.repository.PromotionWithdrawalMapper;
import com.jm.vote.repository.SystemConfigMapper;
import com.jm.vote.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMallService {

    private final MerchantMapper merchantMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final OrderMapper orderMapper;
    private final UserMapper userMapper;
    private final MerchantAuditService merchantAuditService;
    private final com.jm.vote.util.SecurityUtil securityUtil;
    private final PromotionWithdrawalMapper promotionWithdrawalMapper;
    private final PromotionService promotionService;

    // ==================== 商家管理 ====================

    public Page<MerchantListDTO> getMerchants(int page, int size, Integer status) {
        LambdaQueryWrapper<Merchant> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Merchant::getDeleted, 0);
        if (status != null) {
            wrapper.eq(Merchant::getStatus, status);
        }
        wrapper.orderByDesc(Merchant::getCreatedAt);
        Page<Merchant> merchantPage = new Page<>(page, size);
        Page<Merchant> result = merchantMapper.selectPage(merchantPage, wrapper);
        Page<MerchantListDTO> dtoPage = new Page<>(page, size, result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toMerchantListDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    public MerchantDetailDTO getMerchant(Long id) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getId, id)
                        .eq(Merchant::getDeleted, 0)
        );
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        return toMerchantDetailDTO(merchant);
    }

    @Transactional
    public MerchantDetailDTO auditMerchant(Long id, AuditMerchantRequest request) {
        // 使用 MerchantAuditService 处理审核，确保完整的审核流程（包括邮件通知等）
        Long auditorId = getCurrentAdminId(); // 需要获取当前管理员ID
        if (request.getStatus() == 1) {
            // 审核通过
            merchantAuditService.approve(id, auditorId, request.getAuditRemark());
        } else if (request.getStatus() == 2) {
            // 审核拒绝
            merchantAuditService.reject(id, auditorId, request.getAuditRemark());
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "审核状态不正确");
        }
        return getMerchant(id);
    }
    
    /**
     * 获取当前管理员ID
     */
    private Long getCurrentAdminId() {
        return securityUtil.getCurrentAdminId();
    }

    @Transactional
    public MerchantDetailDTO updateMerchantStatus(Long id, Integer status) {
        Merchant merchant = merchantMapper.selectOne(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getId, id)
                        .eq(Merchant::getDeleted, 0)
        );
        if (merchant == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "商家不存在");
        }
        if (status == null || (status != 1 && status != 3)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "状态值不正确");
        }
        merchant.setStatus(status);
        merchant.setUpdatedAt(LocalDateTime.now());
        merchantMapper.updateById(merchant);
        return toMerchantDetailDTO(merchant);
    }

    private MerchantListDTO toMerchantListDTO(Merchant merchant) {
        MerchantListDTO dto = new MerchantListDTO();
        dto.setId(merchant.getId());
        dto.setUsername(merchant.getUsername());
        dto.setEmail(merchant.getEmail());
        dto.setPhone(merchant.getPhone());
        dto.setShopNameZh(merchant.getShopNameZh());
        dto.setShopNameEn(merchant.getShopNameEn());
        dto.setLogo(merchant.getLogo());
        dto.setContactName(merchant.getContactName());
        dto.setContactPhone(merchant.getContactPhone());
        dto.setInviteCode(merchant.getInviteCode());
        dto.setPromotionCode(merchant.getPromotionCode());
        dto.setReferrerCode(merchant.getReferrerCode());
        dto.setStatus(merchant.getStatus());
        dto.setPaymentStatus(merchant.getPaymentStatus());
        dto.setPaymentAmount(merchant.getPaymentAmount());
        dto.setPaymentTime(merchant.getPaymentTime());
        dto.setAuditRemark(merchant.getAuditRemark());
        dto.setAuditTime(merchant.getAuditTime());
        dto.setAuditorId(merchant.getAuditorId());
        dto.setCreatedAt(merchant.getCreatedAt());
        dto.setUpdatedAt(merchant.getUpdatedAt());
        return dto;
    }

    private MerchantDetailDTO toMerchantDetailDTO(Merchant merchant) {
        MerchantDetailDTO dto = new MerchantDetailDTO();
        dto.setId(merchant.getId());
        dto.setUsername(merchant.getUsername());
        dto.setEmail(merchant.getEmail());
        dto.setPhone(merchant.getPhone());
        dto.setShopNameZh(merchant.getShopNameZh());
        dto.setShopNameEn(merchant.getShopNameEn());
        dto.setLogo(merchant.getLogo());
        dto.setBanner(merchant.getBanner());
        dto.setDescriptionZh(merchant.getDescriptionZh());
        dto.setDescriptionEn(merchant.getDescriptionEn());
        dto.setContactName(merchant.getContactName());
        dto.setContactPhone(merchant.getContactPhone());
        dto.setBusinessHours(merchant.getBusinessHours());
        dto.setInviteCode(merchant.getInviteCode());
        dto.setPromotionCode(merchant.getPromotionCode());
        dto.setReferrerCode(merchant.getReferrerCode());
        dto.setStatus(merchant.getStatus());
        dto.setPaymentStatus(merchant.getPaymentStatus());
        dto.setPaymentAmount(merchant.getPaymentAmount());
        dto.setPaymentTime(merchant.getPaymentTime());
        dto.setAuditRemark(merchant.getAuditRemark());
        dto.setAuditTime(merchant.getAuditTime());
        dto.setAuditorId(merchant.getAuditorId());
        dto.setServiceWechat(merchant.getServiceWechat());
        dto.setServicePhone(merchant.getServicePhone());
        dto.setCreatedAt(merchant.getCreatedAt());
        dto.setUpdatedAt(merchant.getUpdatedAt());
        return dto;
    }

    // ==================== 系统配置管理 ====================

    public List<SystemConfigDTO> getSystemConfigs(String configGroup) {
        LambdaQueryWrapper<SystemConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SystemConfig::getDeleted, 0);
        if (configGroup != null && !configGroup.isEmpty()) {
            wrapper.eq(SystemConfig::getConfigGroup, configGroup);
        }
        wrapper.orderByAsc(SystemConfig::getSort);
        List<SystemConfig> configs = systemConfigMapper.selectList(wrapper);
        return configs.stream().map(this::toSystemConfigDTO).collect(Collectors.toList());
    }

    public SystemConfigDTO getSystemConfig(Long id) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getId, id)
                        .eq(SystemConfig::getDeleted, 0)
        );
        if (config == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "配置不存在");
        }
        return toSystemConfigDTO(config);
    }

    public SystemConfigDTO getSystemConfigByKey(String configKey) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, configKey)
                        .eq(SystemConfig::getDeleted, 0)
        );
        if (config == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "配置不存在");
        }
        return toSystemConfigDTO(config);
    }

    public SystemConfigDTO createSystemConfig(CreateSystemConfigRequest request) {
        // 检查configKey是否已存在
        SystemConfig existing = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getConfigKey, request.getConfigKey())
                        .eq(SystemConfig::getDeleted, 0)
        );
        if (existing != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "配置键已存在");
        }
        SystemConfig config = new SystemConfig();
        config.setConfigKey(request.getConfigKey());
        config.setConfigValue(request.getConfigValue());
        config.setNameZh(request.getNameZh());
        config.setNameEn(request.getNameEn());
        config.setDescription(request.getDescription());
        config.setConfigType(request.getConfigType());
        config.setConfigGroup(request.getConfigGroup());
        config.setSort(request.getSort() != null ? request.getSort() : 0);
        config.setDeleted(0);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        systemConfigMapper.insert(config);
        return toSystemConfigDTO(config);
    }

    public SystemConfigDTO updateSystemConfig(Long id, UpdateSystemConfigRequest request) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getId, id)
                        .eq(SystemConfig::getDeleted, 0)
        );
        if (config == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "配置不存在");
        }
        if (request.getConfigValue() != null) config.setConfigValue(request.getConfigValue());
        if (request.getNameZh() != null) config.setNameZh(request.getNameZh());
        if (request.getNameEn() != null) config.setNameEn(request.getNameEn());
        if (request.getDescription() != null) config.setDescription(request.getDescription());
        if (request.getConfigType() != null) config.setConfigType(request.getConfigType());
        if (request.getConfigGroup() != null) config.setConfigGroup(request.getConfigGroup());
        if (request.getSort() != null) config.setSort(request.getSort());
        config.setUpdatedAt(LocalDateTime.now());
        systemConfigMapper.updateById(config);
        return toSystemConfigDTO(config);
    }

    public void deleteSystemConfig(Long id) {
        SystemConfig config = systemConfigMapper.selectOne(
                new LambdaQueryWrapper<SystemConfig>()
                        .eq(SystemConfig::getId, id)
                        .eq(SystemConfig::getDeleted, 0)
        );
        if (config == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "配置不存在");
        }
        config.setDeleted(1);
        config.setUpdatedAt(LocalDateTime.now());
        systemConfigMapper.updateById(config);
    }

    private SystemConfigDTO toSystemConfigDTO(SystemConfig config) {
        SystemConfigDTO dto = new SystemConfigDTO();
        dto.setId(config.getId());
        dto.setConfigKey(config.getConfigKey());
        dto.setConfigValue(config.getConfigValue());
        dto.setNameZh(config.getNameZh());
        dto.setNameEn(config.getNameEn());
        dto.setDescription(config.getDescription());
        dto.setConfigType(config.getConfigType());
        dto.setConfigGroup(config.getConfigGroup());
        dto.setSort(config.getSort());
        dto.setCreatedAt(config.getCreatedAt());
        dto.setUpdatedAt(config.getUpdatedAt());
        return dto;
    }

    // ==================== 数据统计 ====================

    public AdminStatisticsDTO getStatistics() {
        AdminStatisticsDTO stats = new AdminStatisticsDTO();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDate monthStart = today.withDayOfMonth(1);
        
        // 商家统计
        stats.setTotalMerchantCount(merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getDeleted, 0)
        ));
        stats.setPendingMerchantCount(merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getDeleted, 0)
                        .eq(Merchant::getStatus, 0)
        ));
        stats.setActiveMerchantCount(merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getDeleted, 0)
                        .eq(Merchant::getStatus, 1)
        ));
        stats.setDisabledMerchantCount(merchantMapper.selectCount(
                new LambdaQueryWrapper<Merchant>()
                        .eq(Merchant::getDeleted, 0)
                        .eq(Merchant::getStatus, 3)
        ));
        
        // 用户统计
        stats.setTotalUserCount(userMapper.selectCount(
                new LambdaQueryWrapper<User>()
                        .eq(User::getDeleted, 0)
                        .eq(User::getStatus, 1)
        ));
        
        // 订单统计
        stats.setTotalOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeleted, 0)
        ));
        stats.setTodayOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeleted, 0)
                        .ge(Order::getCreatedAt, today.atStartOfDay())
        ));
        stats.setWeekOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeleted, 0)
                        .ge(Order::getCreatedAt, weekStart.atStartOfDay())
        ));
        stats.setMonthOrderCount(orderMapper.selectCount(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getDeleted, 0)
                        .ge(Order::getCreatedAt, monthStart.atStartOfDay())
        ));
        
        return stats;
    }

    // ==================== 提现管理 ====================

    /**
     * 分页获取提现申请列表
     */
    public Page<PromotionWithdrawalDTO> getPromotionWithdrawals(int page, int size, Integer status) {
        Page<PromotionWithdrawal> pageParam = new Page<>(page, size);

        LambdaQueryWrapper<PromotionWithdrawal> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(PromotionWithdrawal::getStatus, status);
        }
        wrapper.orderByDesc(PromotionWithdrawal::getCreatedAt);

        Page<PromotionWithdrawal> withdrawalPage = promotionWithdrawalMapper.selectPage(pageParam, wrapper);

        Page<PromotionWithdrawalDTO> dtoPage = new Page<>(withdrawalPage.getCurrent(), withdrawalPage.getSize(), withdrawalPage.getTotal());

        List<PromotionWithdrawalDTO> dtoList = withdrawalPage.getRecords().stream().map(w -> {
            PromotionWithdrawalDTO dto = new PromotionWithdrawalDTO();
            dto.setId(w.getId());
            dto.setMerchantId(w.getMerchantId());
            dto.setWithdrawalNo(w.getWithdrawalNo());
            dto.setAmount(w.getAmount());
            dto.setFee(w.getFee());
            dto.setActualAmount(w.getActualAmount());
            dto.setAccountType(w.getAccountType());
            dto.setAccountName(w.getAccountName());
            dto.setAccountNumber(w.getAccountNumber());
            dto.setStatus(w.getStatus());
            dto.setAuditRemark(w.getAuditRemark());
            dto.setAuditorId(w.getAuditorId());
            dto.setAuditTime(w.getAuditTime());
            dto.setTransferTime(w.getTransferTime());
            dto.setTransferVoucher(w.getTransferVoucher());
            dto.setCreatedAt(w.getCreatedAt());
            dto.setUpdatedAt(w.getUpdatedAt());

            // 填充商家名称
            Merchant merchant = merchantMapper.selectById(w.getMerchantId());
            if (merchant != null) {
                if (merchant.getShopNameZh() != null && !merchant.getShopNameZh().trim().isEmpty()) {
                    dto.setMerchantName(merchant.getShopNameZh());
                } else if (merchant.getShopNameEn() != null && !merchant.getShopNameEn().trim().isEmpty()) {
                    dto.setMerchantName(merchant.getShopNameEn());
                } else {
                    dto.setMerchantName(merchant.getUsername());
                }
            }

            return dto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(dtoList);
        return dtoPage;
    }

    /**
     * 审核提现申请（通过/拒绝）
     */
    @Transactional
    public java.util.Map<String, Object> auditPromotionWithdrawal(Long withdrawalId, boolean approved, String remark) {
        Long auditorId = getCurrentAdminId();
        return promotionService.auditWithdrawal(withdrawalId, auditorId, approved, remark);
    }

    /**
     * 确认打款
     */
    @Transactional
    public java.util.Map<String, Object> confirmPromotionWithdrawalTransfer(Long withdrawalId, String transferVoucher) {
        return promotionService.confirmTransfer(withdrawalId, transferVoucher);
    }
}

