package com.ecommerce.repository;

import com.ecommerce.entity.User;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Mapper
public interface UserRepository {

    @Select("SELECT id, email, password_hash, role, created_at FROM users WHERE email = #{email}")
    Optional<User> findByEmail(String email);

    @Update("UPDATE users SET password_hash = #{passwordHash} WHERE email = #{email}")
    int updatePasswordHash(@Param("email") String email, @Param("passwordHash") String passwordHash);

    @Insert("INSERT INTO users (email, password_hash, role) VALUES (#{email}, #{passwordHash}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
}
