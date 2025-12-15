#!/bin/bash
set -e

echo "🐳 Building Docker Images"
echo "========================="

VERSION=${1:-latest}

# Build JVM image
echo ""
echo "📦 Building JVM image..."
./mvnw clean package -DskipTests

docker build \
  -f src/main/docker/Dockerfile.jvm \
  -t oauth2-server:${VERSION}-jvm \
  .

# Build Native image
echo ""
echo "🚀 Building Native image..."
./mvnw clean package -Pnative \
  -DskipTests \
  -Dquarkus.native.container-build=true

docker build \
  -f src/main/docker/Dockerfile.native \
  -t oauth2-server:${VERSION}-native \
  .

echo ""
echo "✅ Images built successfully!"
echo ""
echo "📊 Image sizes:"
docker images | grep oauth2-server
echo ""
echo "🎉 You can now run:"
echo "  docker run -p 8080:8080 oauth2-server:${VERSION}-jvm"
echo "  docker run -p 8080:8080 oauth2-server:${VERSION}-native"