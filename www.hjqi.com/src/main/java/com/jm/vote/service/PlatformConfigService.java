package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.PlatformConfigDTO;
import com.jm.vote.entity.PlatformConfig;
import com.jm.vote.repository.PlatformConfigMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlatformConfigService {

    private final PlatformConfigMapper platformConfigMapper;

    /**
     * 获取平台配置列表
     */
    public Page<PlatformConfigDTO> getConfigs(String configType, String configGroup, int page, int size) {
        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformConfig::getDeleted, 0);
        if (configType != null && !configType.isEmpty()) {
            wrapper.eq(PlatformConfig::getConfigType, configType);
        }
        if (configGroup != null && !configGroup.isEmpty()) {
            wrapper.eq(PlatformConfig::getConfigGroup, configGroup);
        }
        wrapper.orderByAsc(PlatformConfig::getSort).orderByDesc(PlatformConfig::getCreatedAt);
        
        Page<PlatformConfig> configPage = new Page<>(page, size);
        Page<PlatformConfig> result = platformConfigMapper.selectPage(configPage, wrapper);
        
        Page<PlatformConfigDTO> dtoPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        dtoPage.setRecords(result.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList()));
        return dtoPage;
    }

    /**
     * 根据配置键获取配置
     */
    public PlatformConfigDTO getConfigByKey(String configKey) {
        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformConfig::getConfigKey, configKey)
                .eq(PlatformConfig::getDeleted, 0)
                .last("LIMIT 1");
        PlatformConfig config = platformConfigMapper.selectOne(wrapper);
        if (config == null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "配置不存在");
        }
        return toDTO(config);
    }

    /**
     * 创建配置
     */
    @Transactional
    public PlatformConfigDTO createConfig(PlatformConfigDTO dto) {
        // 检查configKey是否已存在
        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformConfig::getConfigKey, dto.getConfigKey())
                .eq(PlatformConfig::getDeleted, 0);
        if (platformConfigMapper.selectOne(wrapper) != null) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "配置键已存在");
        }

        PlatformConfig config = new PlatformConfig();
        config.setConfigKey(dto.getConfigKey());
        config.setConfigValue(dto.getConfigValue());
        config.setNameZh(dto.getNameZh());
        config.setNameEn(dto.getNameEn());
        config.setDescription(dto.getDescription());
        config.setConfigType(dto.getConfigType());
        config.setConfigGroup(dto.getConfigGroup());
        config.setSort(dto.getSort() != null ? dto.getSort() : 0);
        config.setCreatedAt(LocalDateTime.now());
        config.setUpdatedAt(LocalDateTime.now());
        config.setDeleted(0);

        platformConfigMapper.insert(config);
        return toDTO(config);
    }

    /**
     * 更新配置
     */
    @Transactional
    public PlatformConfigDTO updateConfig(Long id, PlatformConfigDTO dto) {
        PlatformConfig config = platformConfigMapper.selectById(id);
        if (config == null || config.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "配置不存在");
        }

        if (dto.getConfigValue() != null) {
            config.setConfigValue(dto.getConfigValue());
        }
        if (dto.getNameZh() != null) {
            config.setNameZh(dto.getNameZh());
        }
        if (dto.getNameEn() != null) {
            config.setNameEn(dto.getNameEn());
        }
        if (dto.getDescription() != null) {
            config.setDescription(dto.getDescription());
        }
        if (dto.getConfigType() != null) {
            config.setConfigType(dto.getConfigType());
        }
        if (dto.getConfigGroup() != null) {
            config.setConfigGroup(dto.getConfigGroup());
        }
        if (dto.getSort() != null) {
            config.setSort(dto.getSort());
        }
        config.setUpdatedAt(LocalDateTime.now());

        platformConfigMapper.updateById(config);
        return toDTO(config);
    }

    /**
     * 删除配置
     */
    @Transactional
    public void deleteConfig(Long id) {
        PlatformConfig config = platformConfigMapper.selectById(id);
        if (config == null || config.getDeleted() == 1) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "配置不存在");
        }
        config.setDeleted(1);
        config.setUpdatedAt(LocalDateTime.now());
        platformConfigMapper.updateById(config);
    }

    private PlatformConfigDTO toDTO(PlatformConfig config) {
        PlatformConfigDTO dto = new PlatformConfigDTO();
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

    // ==================== 语言设置相关方法 ====================

    private static final String PLATFORM_LOCALE_KEY = "platform.locale";
    private static final String DEFAULT_LOCALE = "zh-CN";

    /**
     * 获取平台语言设置
     */
    public String getPlatformLocale() {
        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformConfig::getConfigKey, PLATFORM_LOCALE_KEY)
                .eq(PlatformConfig::getDeleted, 0)
                .last("LIMIT 1");
        PlatformConfig config = platformConfigMapper.selectOne(wrapper);
        if (config == null) {
            return DEFAULT_LOCALE;
        }
        return config.getConfigValue();
    }

    /**
     * 设置平台语言
     * @param locale 语言代码：zh-CN 或 en-US
     */
    @Transactional
    public void setPlatformLocale(String locale) {
        // 验证语言代码
        if (!"zh-CN".equals(locale) && !"en-US".equals(locale)) {
            throw new ResponseStatusException(org.springframework.http.HttpStatus.BAD_REQUEST, "不支持的语言代码");
        }

        LambdaQueryWrapper<PlatformConfig> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PlatformConfig::getConfigKey, PLATFORM_LOCALE_KEY)
                .eq(PlatformConfig::getDeleted, 0)
                .last("LIMIT 1");
        PlatformConfig config = platformConfigMapper.selectOne(wrapper);

        if (config == null) {
            // 创建新配置
            config = new PlatformConfig();
            config.setConfigKey(PLATFORM_LOCALE_KEY);
            config.setConfigValue(locale);
            config.setNameZh("平台语言设置");
            config.setNameEn("Platform Language Setting");
            config.setDescription("控制整个平台的显示语言");
            config.setConfigType("BASIC_INFO");
            config.setConfigGroup("LANGUAGE");
            config.setSort(0);
            config.setCreatedAt(LocalDateTime.now());
            config.setUpdatedAt(LocalDateTime.now());
            config.setDeleted(0);
            platformConfigMapper.insert(config);
        } else {
            // 更新现有配置
            config.setConfigValue(locale);
            config.setUpdatedAt(LocalDateTime.now());
            platformConfigMapper.updateById(config);
        }
    }
}

