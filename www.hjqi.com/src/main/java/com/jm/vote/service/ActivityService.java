package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.ActivityDTO;
import com.jm.vote.dto.CreateActivityRequest;
import com.jm.vote.dto.UpdateActivityRequest;
import com.jm.vote.entity.Activity;
import com.jm.vote.entity.Merchant;
import com.jm.vote.repository.ActivityMapper;
import com.jm.vote.repository.MerchantMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final ActivityMapper activityMapper;
    private final MerchantMapper merchantMapper;

    /**
     * 获取活动列表
     */
    public Page<ActivityDTO> getActivities(String activityType, Integer status, int page, int size) {
        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Activity::getDeleted, 0);
        if (activityType != null && !activityType.isEmpty()) {
            wrapper.eq(Activity::getActivityType, activityType);
        }
        if (status != null) {
            wrapper.eq(Activity::getStatus, status);
        }
        wrapper.orderByDesc(Activity::getSort).orderByDesc(Activity::getCreatedAt);
        
        Page<Activity> activityPage = new Page<>(page, size);
        Page<Activity> result = activityMapper.selectPage(activityPage, wrapper);
        
        Page<ActivityDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
        return dtoPage;
    }

    /**
     * 获取活动详情
     */
    public ActivityDTO getActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null || activity.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "活动不存在");
        }
        return toDTO(activity);
    }

    /**
     * 创建活动
     */
    @Transactional
    public ActivityDTO createActivity(CreateActivityRequest request) {
        Activity activity = new Activity();
        activity.setActivityNo("ACT" + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        activity.setNameZh(request.getNameZh());
        activity.setNameEn(request.getNameEn());
        activity.setDescriptionZh(request.getDescriptionZh());
        activity.setDescriptionEn(request.getDescriptionEn());
        activity.setActivityType(request.getActivityType());
        activity.setCoverImage(request.getCoverImage());
        activity.setH5Url(request.getH5Url());
        activity.setPcUrl(request.getPcUrl());
        activity.setStartTime(request.getStartTime());
        activity.setEndTime(request.getEndTime());
        activity.setStatus(calculateStatus(request.getStartTime(), request.getEndTime()));
        activity.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        activity.setSort(request.getSort() != null ? request.getSort() : 0);
        activity.setMerchantId(request.getMerchantId());
        activity.setCreatedAt(LocalDateTime.now());
        activity.setUpdatedAt(LocalDateTime.now());
        activity.setDeleted(0);

        activityMapper.insert(activity);
        return toDTO(activity);
    }

    /**
     * 更新活动
     */
    @Transactional
    public ActivityDTO updateActivity(Long id, UpdateActivityRequest request) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null || activity.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "活动不存在");
        }

        if (request.getNameZh() != null) {
            activity.setNameZh(request.getNameZh());
        }
        if (request.getNameEn() != null) {
            activity.setNameEn(request.getNameEn());
        }
        if (request.getDescriptionZh() != null) {
            activity.setDescriptionZh(request.getDescriptionZh());
        }
        if (request.getDescriptionEn() != null) {
            activity.setDescriptionEn(request.getDescriptionEn());
        }
        if (request.getActivityType() != null) {
            activity.setActivityType(request.getActivityType());
        }
        if (request.getCoverImage() != null) {
            activity.setCoverImage(request.getCoverImage());
        }
        if (request.getH5Url() != null) {
            activity.setH5Url(request.getH5Url());
        }
        if (request.getPcUrl() != null) {
            activity.setPcUrl(request.getPcUrl());
        }
        if (request.getStartTime() != null) {
            activity.setStartTime(request.getStartTime());
        }
        if (request.getEndTime() != null) {
            activity.setEndTime(request.getEndTime());
        }
        if (request.getEnabled() != null) {
            activity.setEnabled(request.getEnabled());
        }
        if (request.getSort() != null) {
            activity.setSort(request.getSort());
        }
        if (request.getMerchantId() != null) {
            activity.setMerchantId(request.getMerchantId());
        }
        
        // 重新计算状态
        activity.setStatus(calculateStatus(activity.getStartTime(), activity.getEndTime()));
        activity.setUpdatedAt(LocalDateTime.now());

        activityMapper.updateById(activity);
        return toDTO(activity);
    }

    /**
     * 删除活动
     */
    @Transactional
    public void deleteActivity(Long id) {
        Activity activity = activityMapper.selectById(id);
        if (activity == null || activity.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "活动不存在");
        }
        activity.setDeleted(1);
        activity.setUpdatedAt(LocalDateTime.now());
        activityMapper.updateById(activity);
    }

    /**
     * 计算活动状态：0-未开始 1-进行中 2-已结束 3-已取消
     */
    private Integer calculateStatus(LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(startTime)) {
            return 0; // 未开始
        } else if (now.isAfter(endTime)) {
            return 2; // 已结束
        } else {
            return 1; // 进行中
        }
    }

    private ActivityDTO toDTO(Activity activity) {
        ActivityDTO dto = new ActivityDTO();
        dto.setId(activity.getId());
        dto.setActivityNo(activity.getActivityNo());
        dto.setNameZh(activity.getNameZh());
        dto.setNameEn(activity.getNameEn());
        dto.setDescriptionZh(activity.getDescriptionZh());
        dto.setDescriptionEn(activity.getDescriptionEn());
        dto.setActivityType(activity.getActivityType());
        dto.setCoverImage(activity.getCoverImage());
        dto.setH5Url(activity.getH5Url());
        dto.setPcUrl(activity.getPcUrl());
        dto.setStartTime(activity.getStartTime());
        dto.setEndTime(activity.getEndTime());
        dto.setStatus(activity.getStatus());
        dto.setEnabled(activity.getEnabled());
        dto.setSort(activity.getSort());
        dto.setMerchantId(activity.getMerchantId());
        dto.setCreatedAt(activity.getCreatedAt());
        dto.setUpdatedAt(activity.getUpdatedAt());
        
        // 查询商家名称
        if (activity.getMerchantId() != null) {
            Merchant merchant = merchantMapper.selectById(activity.getMerchantId());
            if (merchant != null) {
                dto.setMerchantName(merchant.getShopNameZh() != null ? merchant.getShopNameZh() : merchant.getShopNameEn());
            }
        }
        
        return dto;
    }
}

