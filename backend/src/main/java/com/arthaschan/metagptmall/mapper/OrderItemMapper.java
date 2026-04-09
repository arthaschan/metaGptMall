package com.arthaschan.metagptmall.mapper;

import com.arthaschan.metagptmall.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface OrderItemMapper {
    void insertBatch(List<OrderItem> items);
}
