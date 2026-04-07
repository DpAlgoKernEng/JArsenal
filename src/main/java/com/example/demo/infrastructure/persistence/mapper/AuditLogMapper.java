package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.infrastructure.persistence.po.AuditLogPO;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 审计日志 Mapper 接口
 */
@Mapper
public interface AuditLogMapper {

    @Insert("INSERT INTO audit_log(user_id, username, operation, module, description, target_id, ip, trace_id, status, error_msg, duration, created_at) " +
            "VALUES(#{userId}, #{username}, #{operation}, #{module}, #{description}, #{targetId}, #{ip}, #{traceId}, #{status}, #{errorMsg}, #{duration}, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(AuditLogPO log);

    @Select("SELECT id, user_id, username, operation, module, description, target_id, ip, trace_id, status, error_msg, duration, created_at " +
            "FROM audit_log WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<AuditLogPO> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT id, user_id, username, operation, module, description, target_id, ip, trace_id, status, error_msg, duration, created_at " +
            "FROM audit_log WHERE trace_id = #{traceId}")
    AuditLogPO selectByTraceId(@Param("traceId") String traceId);
}