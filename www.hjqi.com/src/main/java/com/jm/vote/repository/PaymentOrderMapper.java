package com.jm.vote.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jm.vote.entity.PaymentOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface PaymentOrderMapper extends BaseMapper<PaymentOrder> {
}

