#!/bin/sh

# 启动 nginx (前端静态文件服务)
nginx

# 启动 Spring Boot 后端
java -jar /app/app.jar \
    -Dspring.profiles.active=prod \
    -Dserver.port=8080 \
    -DJWT_SECRET=${JWT_SECRET} \
    -DDB_URL=${DB_URL} \
    -DDB_USERNAME=${DB_USERNAME} \
    -DDB_PASSWORD=${DB_PASSWORD} \
    -DREDIS_HOST=${REDIS_HOST} \
    -DREDIS_PORT=${REDIS_PORT} \
    -DREDIS_PASSWORD=${REDIS_PASSWORD} \
    -DKAFKA_BOOTSTRAP_SERVERS=${KAFKA_BOOTSTRAP_SERVERS} \
    -DCORS_ALLOWED_ORIGINS=${CORS_ALLOWED_ORIGINS}