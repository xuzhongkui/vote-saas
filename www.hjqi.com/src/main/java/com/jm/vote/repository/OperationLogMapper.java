package com.jm.vote.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jm.vote.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}

