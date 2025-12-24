#!/bin/bash

set -e

echo "🔨 Building OAuth2 Server - Native Image"

# Clean
echo "📦 Cleaning..."
./mvnw clean

# Build native image
echo "🏗️  Building native executable..."
./mvnw package -Dnative -DskipTests

# Check if build succeeded
if [ ! -f target/*-runner ]; then
    echo "❌ Native build failed - executable not found"
    exit 1
fi

echo "✅ Native executable built successfully"
ls -lh target/*-runner

# Build Docker image
echo "🐳 Building Docker image..."
docker build \
    -f src/main/docker/Dockerfile.native \
    -t ghcr.io/hatrongvu13/oauth2-server:latest \
    .

echo "✅ Docker image built successfully"
docker images | grep oauth2-server

# Optional: Push to registry
read -p "Push to registry? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    echo "📤 Pushing to registry..."
    docker push ghcr.io/hatrongvu13/oauth2-server:latest
    echo "✅ Pushed successfully"
fi

echo "🎉 Build complete!"