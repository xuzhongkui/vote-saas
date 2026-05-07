package com.jm.vote.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.jm.vote.entity.CartItem;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CartItemMapper extends BaseMapper<CartItem> {
}


