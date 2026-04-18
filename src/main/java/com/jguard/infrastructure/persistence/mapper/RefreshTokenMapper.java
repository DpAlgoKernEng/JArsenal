package com.jguard.infrastructure.persistence.mapper;

import com.jguard.infrastructure.persistence.po.RefreshTokenPO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Refresh Token Mapper 接口
 */
@Mapper
public interface RefreshTokenMapper {

    @Select("SELECT id, user_id, token, expires_at, create_time, revoked FROM refresh_token WHERE token = #{token}")
    RefreshTokenPO selectByToken(@Param("token") String token);

    @Insert("INSERT INTO refresh_token(user_id, token, expires_at, create_time, revoked) " +
            "VALUES(#{userId}, #{token}, #{expiresAt}, NOW(), 0)")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(RefreshTokenPO token);

    @Update("UPDATE refresh_token SET revoked = 1 WHERE id = #{id}")
    int revokeById(@Param("id") Long id);

    @Update("UPDATE refresh_token SET revoked = 1 WHERE user_id = #{userId}")
    int revokeByUserId(@Param("userId") Long userId);

    @Update("UPDATE refresh_token SET revoked = 1 WHERE token = #{token}")
    int revokeByToken(@Param("token") String token);

    @Delete("DELETE FROM refresh_token WHERE expires_at < NOW()")
    int deleteExpired();

    @Select("SELECT id, user_id, token, expires_at, create_time, revoked FROM refresh_token WHERE user_id = #{userId} AND revoked = 0")
    List<RefreshTokenPO> selectValidByUserId(@Param("userId") Long userId);
}