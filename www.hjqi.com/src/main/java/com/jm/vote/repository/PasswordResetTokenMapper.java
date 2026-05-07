package com.jm.vote.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jm.vote.entity.PasswordResetToken;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PasswordResetTokenMapper extends BaseMapper<PasswordResetToken> {
}

