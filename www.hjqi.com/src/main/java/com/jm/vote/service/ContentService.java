package com.jm.vote.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.jm.vote.dto.ContentDTO;
import com.jm.vote.dto.CreateContentRequest;
import com.jm.vote.dto.UpdateContentRequest;
import com.jm.vote.entity.Content;
import com.jm.vote.repository.ContentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ContentService {

    private final ContentMapper contentMapper;

    public Page<ContentDTO> getContents(String contentType, int page, int size, Integer status) {
        LambdaQueryWrapper<Content> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Content::getDeleted, 0);
        if (contentType != null && !contentType.isEmpty()) {
            wrapper.eq(Content::getContentType, contentType);
        }
        if (status != null) {
            wrapper.eq(Content::getStatus, status);
        }
        wrapper.orderByAsc(Content::getSort)
                .orderByDesc(Content::getCreatedAt);
        
        Page<Content> contentPage = new Page<>(page, size);
        Page<Content> result = contentMapper.selectPage(contentPage, wrapper);
        Page<ContentDTO> dtoPage = new Page<>(page, size, result.getTotal());
        dtoPage.setRecords(result.getRecords().stream().map(this::toContentDTO).collect(Collectors.toList()));
        return dtoPage;
    }

    public ContentDTO getContent(Long id) {
        Content content = contentMapper.selectById(id);
        if (content == null || content.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "内容不存在");
        }
        return toContentDTO(content);
    }

    @Transactional
    public ContentDTO createContent(CreateContentRequest request) {
        Content content = new Content();
        content.setContentType(request.getContentType());
        content.setTitleZh(request.getTitleZh());
        content.setTitleEn(request.getTitleEn());
        content.setContentZh(request.getContentZh());
        content.setContentEn(request.getContentEn());
        content.setCoverImage(request.getCoverImage());
        content.setLinkUrl(request.getLinkUrl());
        content.setSort(request.getSort() != null ? request.getSort() : 0);
        content.setStatus(request.getStatus() != null ? request.getStatus() : 1);
        content.setPublishAt(request.getStatus() != null && request.getStatus() == 1 ? LocalDateTime.now() : null);
        content.setDeleted(0);
        content.setCreatedAt(LocalDateTime.now());
        content.setUpdatedAt(LocalDateTime.now());
        contentMapper.insert(content);
        return toContentDTO(content);
    }

    @Transactional
    public ContentDTO updateContent(Long id, UpdateContentRequest request) {
        Content content = contentMapper.selectById(id);
        if (content == null || content.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "内容不存在");
        }
        
        if (request.getTitleZh() != null) content.setTitleZh(request.getTitleZh());
        if (request.getTitleEn() != null) content.setTitleEn(request.getTitleEn());
        if (request.getContentZh() != null) content.setContentZh(request.getContentZh());
        if (request.getContentEn() != null) content.setContentEn(request.getContentEn());
        if (request.getCoverImage() != null) content.setCoverImage(request.getCoverImage());
        if (request.getLinkUrl() != null) content.setLinkUrl(request.getLinkUrl());
        if (request.getSort() != null) content.setSort(request.getSort());
        if (request.getStatus() != null) {
            content.setStatus(request.getStatus());
            if (request.getStatus() == 1 && content.getPublishAt() == null) {
                content.setPublishAt(LocalDateTime.now());
            }
        }
        content.setUpdatedAt(LocalDateTime.now());
        contentMapper.updateById(content);
        return toContentDTO(content);
    }

    @Transactional
    public void deleteContent(Long id) {
        Content content = contentMapper.selectById(id);
        if (content == null || content.getDeleted() == 1) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "内容不存在");
        }
        content.setDeleted(1);
        content.setUpdatedAt(LocalDateTime.now());
        contentMapper.updateById(content);
    }

    private ContentDTO toContentDTO(Content content) {
        ContentDTO dto = new ContentDTO();
        dto.setId(content.getId());
        dto.setContentType(content.getContentType());
        dto.setTitleZh(content.getTitleZh());
        dto.setTitleEn(content.getTitleEn());
        dto.setContentZh(content.getContentZh());
        dto.setContentEn(content.getContentEn());
        dto.setCoverImage(content.getCoverImage());
        dto.setLinkUrl(content.getLinkUrl());
        dto.setSort(content.getSort());
        dto.setStatus(content.getStatus());
        dto.setPublishAt(content.getPublishAt());
        dto.setCreatedAt(content.getCreatedAt());
        dto.setUpdatedAt(content.getUpdatedAt());
        return dto;
    }
}

