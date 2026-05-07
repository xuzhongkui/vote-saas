package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.AppDownloadDTO;
import com.jm.vote.dto.CreateAppDownloadRequest;
import com.jm.vote.entity.AppDownload;
import com.jm.vote.repository.AppDownloadMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AppDownloadService {

    private final AppDownloadMapper appDownloadMapper;

    /**
     * 获取App下载列表
     */
    public Page<AppDownloadDTO> getAppDownloads(String platform, Integer enabled, int page, int size) {
        LambdaQueryWrapper<AppDownload> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppDownload::getDeleted, 0);
        if (platform != null && !platform.isEmpty()) {
            wrapper.eq(AppDownload::getPlatform, platform);
        }
        if (enabled != null) {
            wrapper.eq(AppDownload::getEnabled, enabled);
        }
        wrapper.orderByDesc(AppDownload::getPublishTime).orderByDesc(AppDownload::getCreatedAt);
        
        Page<AppDownload> downloadPage = new Page<>(page, size);
        Page<AppDownload> result = appDownloadMapper.selectPage(downloadPage, wrapper);
        
        Page<AppDownloadDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
        return dtoPage;
    }

    /**
     * 获取App下载详情
     */
    public AppDownloadDTO getAppDownload(Long id) {
        AppDownload appDownload = appDownloadMapper.selectById(id);
        if (appDownload == null || appDownload.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "App下载信息不存在");
        }
        return toDTO(appDownload);
    }

    /**
     * 获取最新版本的App下载信息（公开接口）
     */
    public AppDownloadDTO getLatestAppDownload(String platform) {
        LambdaQueryWrapper<AppDownload> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppDownload::getPlatform, platform)
                .eq(AppDownload::getEnabled, 1)
                .eq(AppDownload::getDeleted, 0)
                .orderByDesc(AppDownload::getPublishTime)
                .last("LIMIT 1");
        AppDownload appDownload = appDownloadMapper.selectOne(wrapper);
        if (appDownload == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "未找到可用的App下载信息");
        }
        return toDTO(appDownload);
    }

    /**
     * 创建App下载信息
     */
    @Transactional
    public AppDownloadDTO createAppDownload(CreateAppDownloadRequest request) {
        AppDownload appDownload = new AppDownload();
        appDownload.setPlatform(request.getPlatform());
        appDownload.setVersion(request.getVersion());
        appDownload.setVersionName(request.getVersionName());
        appDownload.setDownloadUrl(request.getDownloadUrl());
        appDownload.setUpdateNotesZh(request.getUpdateNotesZh());
        appDownload.setUpdateNotesEn(request.getUpdateNotesEn());
        appDownload.setForceUpdate(request.getForceUpdate() != null ? request.getForceUpdate() : 0);
        appDownload.setFileSize(request.getFileSize());
        appDownload.setFileMd5(request.getFileMd5());
        appDownload.setEnabled(request.getEnabled() != null ? request.getEnabled() : 1);
        appDownload.setPublishTime(request.getPublishTime() != null ? request.getPublishTime() : LocalDateTime.now());
        appDownload.setCreatedAt(LocalDateTime.now());
        appDownload.setUpdatedAt(LocalDateTime.now());
        appDownload.setDeleted(0);

        appDownloadMapper.insert(appDownload);
        return toDTO(appDownload);
    }

    /**
     * 更新App下载信息
     */
    @Transactional
    public AppDownloadDTO updateAppDownload(Long id, CreateAppDownloadRequest request) {
        AppDownload appDownload = appDownloadMapper.selectById(id);
        if (appDownload == null || appDownload.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "App下载信息不存在");
        }

        if (request.getPlatform() != null) {
            appDownload.setPlatform(request.getPlatform());
        }
        if (request.getVersion() != null) {
            appDownload.setVersion(request.getVersion());
        }
        if (request.getVersionName() != null) {
            appDownload.setVersionName(request.getVersionName());
        }
        if (request.getDownloadUrl() != null) {
            appDownload.setDownloadUrl(request.getDownloadUrl());
        }
        if (request.getUpdateNotesZh() != null) {
            appDownload.setUpdateNotesZh(request.getUpdateNotesZh());
        }
        if (request.getUpdateNotesEn() != null) {
            appDownload.setUpdateNotesEn(request.getUpdateNotesEn());
        }
        if (request.getForceUpdate() != null) {
            appDownload.setForceUpdate(request.getForceUpdate());
        }
        if (request.getFileSize() != null) {
            appDownload.setFileSize(request.getFileSize());
        }
        if (request.getFileMd5() != null) {
            appDownload.setFileMd5(request.getFileMd5());
        }
        if (request.getEnabled() != null) {
            appDownload.setEnabled(request.getEnabled());
        }
        if (request.getPublishTime() != null) {
            appDownload.setPublishTime(request.getPublishTime());
        }
        appDownload.setUpdatedAt(LocalDateTime.now());

        appDownloadMapper.updateById(appDownload);
        return toDTO(appDownload);
    }

    /**
     * 删除App下载信息
     */
    @Transactional
    public void deleteAppDownload(Long id) {
        AppDownload appDownload = appDownloadMapper.selectById(id);
        if (appDownload == null || appDownload.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "App下载信息不存在");
        }
        appDownload.setDeleted(1);
        appDownload.setUpdatedAt(LocalDateTime.now());
        appDownloadMapper.updateById(appDownload);
    }

    private AppDownloadDTO toDTO(AppDownload appDownload) {
        AppDownloadDTO dto = new AppDownloadDTO();
        dto.setId(appDownload.getId());
        dto.setPlatform(appDownload.getPlatform());
        dto.setVersion(appDownload.getVersion());
        dto.setVersionName(appDownload.getVersionName());
        dto.setDownloadUrl(appDownload.getDownloadUrl());
        dto.setUpdateNotesZh(appDownload.getUpdateNotesZh());
        dto.setUpdateNotesEn(appDownload.getUpdateNotesEn());
        dto.setForceUpdate(appDownload.getForceUpdate());
        dto.setFileSize(appDownload.getFileSize());
        dto.setFileMd5(appDownload.getFileMd5());
        dto.setEnabled(appDownload.getEnabled());
        dto.setPublishTime(appDownload.getPublishTime());
        dto.setCreatedAt(appDownload.getCreatedAt());
        dto.setUpdatedAt(appDownload.getUpdatedAt());
        return dto;
    }
}

