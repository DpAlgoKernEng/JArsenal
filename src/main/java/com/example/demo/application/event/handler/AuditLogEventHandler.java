package com.example.demo.application.event.handler;

import com.example.demo.domain.audit.aggregate.AuditLog;
import com.example.demo.domain.audit.repository.AuditLogRepository;
import com.example.demo.domain.audit.valueobject.ModuleType;
import com.example.demo.domain.audit.valueobject.OperationType;
import com.example.demo.domain.auth.event.TokenRefreshed;
import com.example.demo.domain.auth.event.UserLoggedIn;
import com.example.demo.domain.auth.event.UserLoggedOut;
import com.example.demo.domain.user.event.UserRegistered;
import com.example.demo.domain.user.event.UserStatusChanged;
import com.example.demo.domain.user.event.UserUpdated;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * 审计日志事件处理器
 * 监听 Kafka 中的领域事件并记录审计日志
 */
@Component
public class AuditLogEventHandler {

    private static final Logger log = LoggerFactory.getLogger(AuditLogEventHandler.class);
    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    public AuditLogEventHandler(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.objectMapper = new ObjectMapper();
    }

    @KafkaListener(topics = "domain-events", groupId = "jarsenal-audit-group", containerFactory = "stringKafkaListenerContainerFactory")
    public void handleDomainEvent(String message) {
        log.debug("Received domain event message: {}", message);

        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.get("eventType").asText();
            JsonNode payload = root.get("payload");

            switch (eventType) {
                case "UserLoggedIn":
                    handleUserLoggedIn(payload);
                    break;
                case "UserLoggedOut":
                    handleUserLoggedOut(payload);
                    break;
                case "TokenRefreshed":
                    handleTokenRefreshed(payload);
                    break;
                case "UserRegistered":
                    handleUserRegistered(payload);
                    break;
                case "UserUpdated":
                    handleUserUpdated(payload);
                    break;
                case "UserStatusChanged":
                    handleUserStatusChanged(payload);
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Failed to handle domain event: {}", message, e);
        }
    }

    private void handleUserRegistered(JsonNode payload) {
        Long userId = payload.has("userId") && !payload.get("userId").isNull()
            ? payload.get("userId").asLong() : null;
        String username = payload.has("username") ? payload.get("username").asText() : null;

        AuditLog auditLog = AuditLog.success(
            userId != null ? new com.example.demo.domain.user.valueobject.UserId(userId) : null,
            username != null ? new com.example.demo.domain.user.valueobject.Username(username) : null,
            OperationType.REGISTER,
            ModuleType.AUTH,
            "用户注册",
            null,
            null,
            0
        );
        auditLogRepository.save(auditLog);
        log.info("Saved audit log for UserRegistered event");
    }

    private void handleUserLoggedIn(JsonNode payload) {
        Long userId = payload.has("userId") && !payload.get("userId").isNull()
            ? payload.get("userId").asLong() : null;
        String username = payload.has("username") ? payload.get("username").asText() : null;

        AuditLog auditLog = AuditLog.success(
            userId != null ? new com.example.demo.domain.user.valueobject.UserId(userId) : null,
            username != null ? new com.example.demo.domain.user.valueobject.Username(username) : null,
            OperationType.LOGIN,
            ModuleType.AUTH,
            "用户登录",
            null,
            null,
            0
        );
        auditLogRepository.save(auditLog);
        log.info("Saved audit log for UserLoggedIn event");
    }

    private void handleUserLoggedOut(JsonNode payload) {
        Long userId = payload.has("userId") && !payload.get("userId").isNull()
            ? payload.get("userId").asLong() : null;

        AuditLog auditLog = AuditLog.success(
            userId != null ? new com.example.demo.domain.user.valueobject.UserId(userId) : null,
            null,
            OperationType.LOGOUT,
            ModuleType.AUTH,
            "用户登出",
            null,
            null,
            0
        );
        auditLogRepository.save(auditLog);
        log.info("Saved audit log for UserLoggedOut event");
    }

    private void handleTokenRefreshed(JsonNode payload) {
        Long userId = payload.has("userId") && !payload.get("userId").isNull()
            ? payload.get("userId").asLong() : null;

        AuditLog auditLog = AuditLog.success(
            userId != null ? new com.example.demo.domain.user.valueobject.UserId(userId) : null,
            null,
            OperationType.UPDATE,
            ModuleType.AUTH,
            "Token刷新",
            null,
            null,
            0
        );
        auditLogRepository.save(auditLog);
        log.info("Saved audit log for TokenRefreshed event");
    }

    private void handleUserUpdated(JsonNode payload) {
        Long userId = payload.has("userId") && !payload.get("userId").isNull()
            ? payload.get("userId").asLong() : null;

        AuditLog auditLog = AuditLog.success(
            userId != null ? new com.example.demo.domain.user.valueobject.UserId(userId) : null,
            null,
            OperationType.UPDATE,
            ModuleType.USER,
            "用户信息更新",
            null,
            null,
            0
        );
        auditLogRepository.save(auditLog);
        log.info("Saved audit log for UserUpdated event");
    }

    private void handleUserStatusChanged(JsonNode payload) {
        Long userId = payload.has("userId") && !payload.get("userId").isNull()
            ? payload.get("userId").asLong() : null;
        String newStatus = payload.has("newStatus") ? payload.get("newStatus").asText() : "UNKNOWN";

        AuditLog auditLog = AuditLog.success(
            userId != null ? new com.example.demo.domain.user.valueobject.UserId(userId) : null,
            null,
            OperationType.UPDATE,
            ModuleType.USER,
            "用户状态变更为: " + newStatus,
            null,
            null,
            0
        );
        auditLogRepository.save(auditLog);
        log.info("Saved audit log for UserStatusChanged event");
    }
}