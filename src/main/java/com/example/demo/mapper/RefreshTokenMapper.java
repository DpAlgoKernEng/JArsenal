package com.example.demo.mapper;

import com.example.demo.entity.RefreshToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * Refresh Token Mapper
 */
@Mapper
public interface RefreshTokenMapper {

    /**
     * 插入 Refresh Token
     */
    int insert(RefreshToken refreshToken);

    /**
     * 根据 Token 查询
     */
    RefreshToken selectByToken(@Param("token") String token);

    /**
     * 根据用户ID查询有效的 Refresh Token
     */
    RefreshToken selectValidByUserId(@Param("userId") Long userId);

    /**
     * 撤销 Token
     */
    int revokeByToken(@Param("token") String token);

    /**
     * 撤销用户所有 Token
     */
    int revokeAllByUserId(@Param("userId") Long userId);

    /**
     * 删除过期 Token
     */
    int deleteExpired();
}