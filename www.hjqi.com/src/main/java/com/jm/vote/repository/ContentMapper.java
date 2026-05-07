package com.jm.vote.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jm.vote.entity.Content;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ContentMapper extends BaseMapper<Content> {
}

