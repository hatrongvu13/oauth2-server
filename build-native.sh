#!/bin/bash
set -e

echo "🚀 Building OAuth2 Server - Native Image"

BUILD_TYPE=${1:-local}
MEMORY=${NATIVE_IMAGE_XMX:-6g}

case $BUILD_TYPE in
  local)
    ./mvnw clean package -Pnative \
      -DskipTests \
      -Dquarkus.native.container-build=false \
      -Dquarkus.native.native-image-xmx=$MEMORY
    ;;

  container)
    ./mvnw clean package -Pnative \
      -DskipTests \
      -Dquarkus.native.container-build=true \
      -Dquarkus.native.builder-image=quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21 \
      -Dquarkus.native.native-image-xmx=$MEMORY
    ;;
esac

echo "✅ Build successful!"
ls -lh target/*-runner