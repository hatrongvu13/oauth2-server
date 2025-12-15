#!/bin/bash
set -e

echo "⚡ Performance Benchmark"
echo "======================="

# Build both versions
echo "Building JVM version..."
./mvnw clean package -DskipTests -Dquarkus.package.jar.type=uber-jar

echo "Building Native version..."
./mvnw clean package -Pnative -DskipTests

# Startup time comparison
echo ""
echo "📊 Startup Time Comparison:"
echo "----------------------------"

echo -n "JVM: "
time java -jar target/quarkus-app/quarkus-run.jar &
JVM_PID=$!
sleep 3
curl -s http://localhost:8080/q/health > /dev/null
kill $JVM_PID
wait $JVM_PID 2>/dev/null

sleep 2

echo -n "Native: "
time ./target/*-runner &
NATIVE_PID=$!
sleep 1
curl -s http://localhost:8080/q/health > /dev/null
kill $NATIVE_PID
wait $NATIVE_PID 2>/dev/null

# Memory usage comparison
echo ""
echo "💾 Memory Usage:"
echo "----------------"
echo "JVM RSS: $(ps -o rss= -p $JVM_PID | awk '{print $1/1024 "MB"}')"
echo "Native RSS: $(ps -o rss= -p $NATIVE_PID | awk '{print $1/1024 "MB"}')"

# Binary size comparison
echo ""
echo "📦 Binary Size:"
echo "---------------"
echo "JVM: $(du -h target/quarkus-app/quarkus-run.jar | cut -f1)"
echo "Native: $(du -h target/*-runner | cut -f1)"