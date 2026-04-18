package com.jguard;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.jguard.infrastructure.persistence.mapper,com.jguard.infrastructure.outbox")
@EnableAsync
@EnableScheduling
public class JGuardApplication {
    public static void main(String[] args) {
        SpringApplication.run(JGuardApplication.class, args);
    }
}