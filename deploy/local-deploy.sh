#!/bin/bash

# ============================================
# 本地 Docker Compose 部署脚本
# ============================================

set -e

echo "=== 本地 Docker Compose 部署 ==="

# 检查 .env 文件
if [ ! -f ".env" ]; then
    echo ">>> 创建默认环境配置..."
    cp .env.example .env
    echo "警告: 请修改 .env 文件中的敏感配置!"
fi

# 检查 Docker
if ! command -v docker &> /dev/null; then
    echo "错误: Docker 未安装"
    exit 1
fi

if ! command -v docker-compose &> /dev/null; then
    echo "错误: Docker Compose 未安装"
    exit 1
fi

# 构建并启动
echo ">>> 构建镜像..."
docker-compose build

echo ">>> 启动服务..."
docker-compose up -d

echo ">>> 等待服务就绪..."
sleep 30

# 检查健康状态
echo ">>> 检查服务状态..."
docker-compose ps

# 显示日志
echo ""
echo "=== 部署完成 ==="
echo "前端: http://localhost"
echo "后端 API: http://localhost:8080/api"
echo "健康检查: http://localhost:8080/actuator/health"
echo ""
echo "查看日志: docker-compose logs -f"