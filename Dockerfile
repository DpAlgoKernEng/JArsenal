# ============================================
# 多阶段构建：后端 Spring Boot 应用
# ============================================
FROM maven:3.9-eclipse-temurin-17 AS backend-builder

WORKDIR /build

# 复制 pom.xml 并下载依赖（利用缓存）
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 复制源码并构建
COPY src ./src
RUN mvn clean package -DskipTests -B

# ============================================
# 前端 Vue 构建
# ============================================
FROM node:20-alpine AS frontend-builder

WORKDIR /build/ui

COPY ui/package*.json ./
RUN npm ci

COPY ui/ ./
RUN npm run build

# ============================================
# 最终运行镜像
# ============================================
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# 安装 nginx 用于前端静态文件
RUN apk add --no-cache nginx && \
    mkdir -p /var/log/nginx /var/lib/nginx/tmp /usr/share/nginx/html

# 复制后端 jar
COPY --from=backend-builder /build/target/*.jar app.jar

# 复制前端构建产物
COPY --from=frontend-builder /build/ui/dist /usr/share/nginx/html

# 复制 nginx 配置
COPY deploy/nginx.conf /etc/nginx/nginx.conf

# 复制启动脚本
COPY deploy/start.sh /app/start.sh
RUN chmod +x /app/start.sh

# 创建日志目录
RUN mkdir -p /var/log/demo

# 暴露端口
EXPOSE 8080 80

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# 启动应用
CMD ["/app/start.sh"]