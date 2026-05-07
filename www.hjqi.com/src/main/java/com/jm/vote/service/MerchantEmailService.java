package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.jm.vote.entity.MerchantEmail;
import com.jm.vote.repository.MerchantEmailMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MerchantEmailService {

    private final MerchantEmailMapper emailMapper;

    /**
     * 获取商家邮箱列表
     */
    public List<MerchantEmail> getEmails(Long merchantId) {
        LambdaQueryWrapper<MerchantEmail> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MerchantEmail::getMerchantId, merchantId)
                .eq(MerchantEmail::getDeleted, 0)
                .orderByDesc(MerchantEmail::getIsDefault)
                .orderByDesc(MerchantEmail::getCreatedAt);
        return emailMapper.selectList(wrapper);
    }

    /**
     * 创建邮箱
     */
    @Transactional
    public MerchantEmail createEmail(Long merchantId, String email, String name, String purpose, Integer isDefault) {
        // 如果设置为默认邮箱，先取消其他默认邮箱
        if (isDefault != null && isDefault == 1) {
            LambdaQueryWrapper<MerchantEmail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MerchantEmail::getMerchantId, merchantId)
                    .eq(MerchantEmail::getIsDefault, 1)
                    .eq(MerchantEmail::getDeleted, 0);
            List<MerchantEmail> defaultEmails = emailMapper.selectList(wrapper);
            for (MerchantEmail e : defaultEmails) {
                e.setIsDefault(0);
                emailMapper.updateById(e);
            }
        }

        MerchantEmail merchantEmail = MerchantEmail.builder()
                .merchantId(merchantId)
                .email(email)
                .name(name)
                .purpose(purpose)
                .isDefault(isDefault != null ? isDefault : 0)
                .status(1)
                .deleted(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        emailMapper.insert(merchantEmail);
        return merchantEmail;
    }

    /**
     * 更新邮箱
     */
    @Transactional
    public MerchantEmail updateEmail(Long id, Long merchantId, String email, String name, String purpose, Integer isDefault, Integer status) {
        MerchantEmail merchantEmail = emailMapper.selectById(id);
        if (merchantEmail == null || !merchantEmail.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("邮箱不存在或无权限");
        }

        // 如果设置为默认邮箱，先取消其他默认邮箱
        if (isDefault != null && isDefault == 1 && merchantEmail.getIsDefault() != 1) {
            LambdaQueryWrapper<MerchantEmail> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MerchantEmail::getMerchantId, merchantId)
                    .eq(MerchantEmail::getIsDefault, 1)
                    .eq(MerchantEmail::getDeleted, 0)
                    .ne(MerchantEmail::getId, id);
            List<MerchantEmail> defaultEmails = emailMapper.selectList(wrapper);
            for (MerchantEmail e : defaultEmails) {
                e.setIsDefault(0);
                emailMapper.updateById(e);
            }
        }

        if (email != null) merchantEmail.setEmail(email);
        if (name != null) merchantEmail.setName(name);
        if (purpose != null) merchantEmail.setPurpose(purpose);
        if (isDefault != null) merchantEmail.setIsDefault(isDefault);
        if (status != null) merchantEmail.setStatus(status);

        merchantEmail.setUpdatedAt(LocalDateTime.now());
        emailMapper.updateById(merchantEmail);
        return merchantEmail;
    }

    /**
     * 删除邮箱
     */
    @Transactional
    public void deleteEmail(Long id, Long merchantId) {
        MerchantEmail merchantEmail = emailMapper.selectById(id);
        if (merchantEmail == null || !merchantEmail.getMerchantId().equals(merchantId)) {
            throw new RuntimeException("邮箱不存在或无权限");
        }

        merchantEmail.setDeleted(1);
        merchantEmail.setUpdatedAt(LocalDateTime.now());
        emailMapper.updateById(merchantEmail);
    }
}
