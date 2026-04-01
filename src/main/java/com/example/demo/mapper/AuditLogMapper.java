package com.example.demo.mapper;

import com.example.demo.entity.AuditLogEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 审计日志 Mapper
 */
@Mapper
public interface AuditLogMapper {

    /**
     * 插入审计日志
     */
    int insert(AuditLogEntity auditLog);

    /**
     * 根据用户ID查询
     */
    List<AuditLogEntity> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据时间范围查询
     */
    List<AuditLogEntity> selectByTimeRange(@Param("startTime") LocalDateTime startTime,
                                            @Param("endTime") LocalDateTime endTime);

    /**
     * 根据操作类型查询
     */
    List<AuditLogEntity> selectByOperation(@Param("operation") String operation);
}