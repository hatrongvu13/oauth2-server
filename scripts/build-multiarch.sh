#!/bin/bash

set -e

echo "🔨 Building OAuth2 Server - Multi-Architecture Native Image"

# Enable Docker BuildKit
export DOCKER_BUILDKIT=1

# Create builder if not exists
docker buildx create --use --name oauth2-builder || docker buildx use oauth2-builder

# Build for AMD64
echo "🏗️  Building for AMD64..."
./mvnw clean package -Dnative -DskipTests \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21

docker buildx build \
  --platform linux/amd64 \
  -f src/main/docker/Dockerfile.native \
  -t ghcr.io/hatrongvu13/oauth2-server:latest-amd64 \
  --load \
  .

# Build for ARM64 (if on ARM machine or using emulation)
echo "🏗️  Building for ARM64..."
./mvnw clean package -Dnative -DskipTests \
  -Dquarkus.native.container-build=true \
  -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21

docker buildx build \
  --platform linux/arm64 \
  -f src/main/docker/Dockerfile.native \
  -t ghcr.io/hatrongvu13/oauth2-server:latest-arm64 \
  --load \
  .

# Create and push manifest
echo "📦 Creating multi-arch manifest..."
docker manifest create ghcr.io/hatrongvu13/oauth2-server:latest \
  --amend ghcr.io/hatrongvu13/oauth2-server:latest-amd64 \
  --amend ghcr.io/hatrongvu13/oauth2-server:latest-arm64

docker manifest push ghcr.io/hatrongvu13/oauth2-server:latest

echo "✅ Multi-arch image pushed successfully"