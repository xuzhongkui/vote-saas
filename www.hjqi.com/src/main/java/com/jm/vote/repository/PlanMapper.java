package com.jm.vote.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jm.vote.entity.Plan;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PlanMapper extends BaseMapper<Plan> {
}

