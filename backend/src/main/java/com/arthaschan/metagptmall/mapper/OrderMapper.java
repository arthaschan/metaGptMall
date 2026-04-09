package com.arthaschan.metagptmall.mapper;

import com.arthaschan.metagptmall.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface OrderMapper {
    void insert(Order order);
    Order findById(@Param("id") Long id);
}
