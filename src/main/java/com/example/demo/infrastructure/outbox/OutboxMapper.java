package com.example.demo.infrastructure.outbox;

import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Outbox Mapper
 */
@Mapper
public interface OutboxMapper {

    @Insert("INSERT INTO event_outbox(event_id, event_type, aggregate_type, aggregate_id, payload, status, retry_count, create_time) " +
            "VALUES(#{eventId}, #{eventType}, #{aggregateType}, #{aggregateId}, #{payload}, 0, 0, NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(OutboxMessage message);

    @Select("SELECT id, event_id, event_type, aggregate_type, aggregate_id, payload, status, retry_count, error_msg, create_time, sent_time " +
            "FROM event_outbox WHERE status = 0 ORDER BY create_time ASC LIMIT #{limit}")
    List<OutboxMessage> selectPending(@Param("limit") int limit);

    @Update("UPDATE event_outbox SET status = 1, sent_time = NOW() WHERE id = #{id}")
    int markAsSent(@Param("id") Long id);

    @Update("UPDATE event_outbox SET status = 2, error_msg = #{errorMsg}, retry_count = retry_count + 1 WHERE id = #{id}")
    int markAsFailed(@Param("id") Long id, @Param("errorMsg") String errorMsg);

    @Delete("DELETE FROM event_outbox WHERE status = 1 AND sent_time < DATE_SUB(NOW(), INTERVAL #{days} DAY)")
    int deleteSentOlderThan(@Param("days") int days);
}