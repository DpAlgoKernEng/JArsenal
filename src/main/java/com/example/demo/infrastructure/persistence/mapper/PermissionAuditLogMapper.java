package com.example.demo.infrastructure.persistence.mapper;

import com.example.demo.domain.permission.entity.PermissionAuditLog;
import org.apache.ibatis.annotations.*;
import java.util.List;

/**
 * 权限审计日志 Mapper 接口
 */
@Mapper
public interface PermissionAuditLogMapper {

    @Insert("INSERT INTO permission_audit_log(operator_id, operator_name, change_type, target_type, target_id, " +
            "before_value, after_value, reason, trace_id, create_time) " +
            "VALUES(#{operatorId}, #{operatorName}, #{changeType}, #{targetType}, #{targetId}, " +
            "#{beforeValue}, #{afterValue}, #{reason}, #{traceId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PermissionAuditLog log);

    @Select("SELECT id, operator_id, operator_name, change_type, target_type, target_id, " +
            "before_value, after_value, reason, trace_id, create_time " +
            "FROM permission_audit_log WHERE target_type = #{targetType} AND target_id = #{targetId} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<PermissionAuditLog> findByTarget(@Param("targetType") String targetType,
                                          @Param("targetId") Long targetId,
                                          @Param("limit") int limit);

    @Select("SELECT id, operator_id, operator_name, change_type, target_type, target_id, " +
            "before_value, after_value, reason, trace_id, create_time " +
            "FROM permission_audit_log WHERE operator_id = #{operatorId} " +
            "ORDER BY create_time DESC LIMIT #{limit}")
    List<PermissionAuditLog> findByOperator(@Param("operatorId") Long operatorId, @Param("limit") int limit);

    @Select("SELECT id, operator_id, operator_name, change_type, target_type, target_id, " +
            "before_value, after_value, reason, trace_id, create_time " +
            "FROM permission_audit_log WHERE change_type = #{changeType} " +
            "AND create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time DESC")
    List<PermissionAuditLog> findByTypeAndTimeRange(@Param("changeType") String changeType,
                                                    @Param("startTime") String startTime,
                                                    @Param("endTime") String endTime);
}