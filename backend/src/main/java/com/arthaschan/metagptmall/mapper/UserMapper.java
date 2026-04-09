package com.arthaschan.metagptmall.mapper;

import com.arthaschan.metagptmall.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByEmail(@Param("email") String email);
    void insert(User user);
}
