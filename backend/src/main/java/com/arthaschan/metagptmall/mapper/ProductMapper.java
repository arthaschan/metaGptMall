package com.arthaschan.metagptmall.mapper;

import com.arthaschan.metagptmall.entity.Product;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ProductMapper {
    List<Product> findActiveProducts(@Param("offset") int offset, @Param("size") int size);
    long countActiveProducts();
    Product findById(@Param("id") Long id);
    int decreaseStock(@Param("id") Long id, @Param("quantity") int quantity);
}
