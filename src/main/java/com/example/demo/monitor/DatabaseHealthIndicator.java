package com.example.demo.monitor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 数据库健康检查
 */
@Slf4j
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(3)) {
                return Health.up()
                        .withDetail("database", "MySQL")
                        .withDetail("validationQuery", "isValid")
                        .build();
            }
        } catch (Exception e) {
            log.error("数据库健康检查失败", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
        return Health.down().withDetail("reason", "连接无效").build();
    }
}