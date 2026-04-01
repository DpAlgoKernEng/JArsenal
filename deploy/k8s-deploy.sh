#!/bin/bash

# ============================================
# Kubernetes 部署脚本
# ============================================

set -e

NAMESPACE="jarsenal"
REGISTRY="${REGISTRY:-ghcr.io}"
IMAGE_NAME="${IMAGE_NAME:-your-org/jarsenal}"
IMAGE_TAG="${IMAGE_TAG:-latest}"

echo "=== 部署 JArsenal 到 Kubernetes ==="
echo "Namespace: $NAMESPACE"
echo "Image: $REGISTRY/$IMAGE_NAME:$IMAGE_TAG"

# 检查 kubectl
if ! command -v kubectl &> /dev/null; then
    echo "错误: kubectl 未安装"
    exit 1
fi

# 检查集群连接
if ! kubectl cluster-info &> /dev/null; then
    echo "错误: 无法连接到 Kubernetes 集群"
    exit 1
fi

# 创建命名空间
echo ">>> 创建命名空间..."
kubectl apply -f k8s/namespace.yaml

# 替换镜像标签
echo ">>> 配置镜像版本..."
sed -i "s|IMAGE_REGISTRY|$REGISTRY/$IMAGE_NAME|g" k8s/deployment.yaml
sed -i "s|IMAGE_TAG|$IMAGE_TAG|g" k8s/deployment.yaml

# 部署基础设施 (MySQL, Redis)
echo ">>> 部署 MySQL..."
kubectl apply -f k8s/mysql.yaml

echo ">>> 部署 Redis..."
kubectl apply -f k8s/redis.yaml

# 等待基础设施就绪
echo ">>> 等待基础设施就绪..."
kubectl wait --for=condition=ready pod -l app=mysql -n $NAMESPACE --timeout=120s || true
kubectl wait --for=condition=ready pod -l app=redis -n $NAMESPACE --timeout=60s || true

# 部署应用
echo ">>> 部署 ConfigMap..."
kubectl apply -f k8s/configmap.yaml

echo ">>> 部署 Secret..."
kubectl apply -f k8s/secret.yaml

echo ">>> 部署应用..."
kubectl apply -f k8s/deployment.yaml

echo ">>> 部署 Service..."
kubectl apply -f k8s/service.yaml

echo ">>> 部署 Ingress..."
kubectl apply -f k8s/ingress.yaml

# 等待部署完成
echo ">>> 等待应用就绪..."
kubectl rollout status deployment/jarsenal -n $NAMESPACE --timeout=180s

# 显示部署状态
echo ""
echo "=== 部署完成 ==="
kubectl get pods -n $NAMESPACE
kubectl get services -n $NAMESPACE
kubectl get ingress -n $NAMESPACE

echo ""
echo "访问地址: https://jarsenal.example.com"