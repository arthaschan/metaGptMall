package com.arthaschan.metagptmall.mapper;

import com.arthaschan.metagptmall.entity.Cart;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CartMapper {
    Cart findByUserId(@Param("userId") Long userId);
    void insert(Cart cart);
}
