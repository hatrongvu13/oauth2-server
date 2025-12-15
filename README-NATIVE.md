# Native Image Build Guide

## Prerequisites

### 1. Install GraalVM

**Using SDKMAN:**
```bash
sdk install java 21.0.9-graalce
sdk use java 21.0.9-graalce
gu install native-image
```

**Manual Installation:**
- Download from: https://github.com/graalvm/mandrel/releases
- Extract and add to PATH
- Install native-image: `gu install native-image`

### 2. System Requirements

- **Memory**: Minimum 8GB RAM (16GB recommended)
- **Disk**: 10GB free space
- **OS**: Linux, macOS, or Windows with WSL2

## Build Methods

### Method 1: Local Build (Fastest)

```bash
# Basic build
./build-native.sh local

# With custom memory
NATIVE_IMAGE_XMX=8g ./build-native.sh local

# Or using Maven directly
./mvnw clean package -Pnative -DskipTests
```

**Pros:**
- Fastest build time
- Better error messages
- Easier debugging

**Cons:**
- Requires GraalVM installation
- Platform-specific binary

### Method 2: Container Build (Portable)

```bash
# Build in Docker container
./build-native.sh container

# Or using Maven
./mvnw clean package -Pnative \
  -DskipTests \
  -Dquarkus.native.container-build=true
```

**Pros:**
- No GraalVM installation needed
- Linux binary (portable)
- Same as CI/CD

**Cons:**
- Slower build
- Requires Docker

### Method 3: Multi-stage Docker Build

```bash
docker build -f src/main/docker/Dockerfile.native -t oauth2-server:native .
```

## Build Options

### Memory Configuration

```bash
# Default: 6GB
-Dquarkus.native.native-image-xmx=6g

# For large applications
-Dquarkus.native.native-image-xmx=8g

# CI/CD environments
-Dquarkus.native.native-image-xmx=4g
```

### Additional Build Args

```properties
# Enable all security services
-Dquarkus.native.enable-all-security-services=true

# Include all charsets
-Dquarkus.native.add-all-charsets=true

# Debug symbols (increases size)
-Dquarkus.native.debug.enabled=true
```

## Troubleshooting

### Issue 1: SecureRandom Error

**Error:**
```
Detected an instance of Random/SplittableRandom class in the image heap
```

**Solution:**
Already fixed in `CryptoUtil.java` using lazy initialization pattern.

### Issue 2: Out of Memory

**Error:**
```
java.lang.OutOfMemoryError: GC overhead limit exceeded
```

**Solution:**
```bash
# Increase memory
export MAVEN_OPTS="-Xmx8g"
./mvnw clean package -Pnative -Dquarkus.native.native-image-xmx=8g
```

### Issue 3: Missing Resources

**Error:**
```
Resource not found: application.yml
```

**Solution:**
Check `src/main/resources/META-INF/native-image/resource-config.json`

### Issue 4: Reflection Errors

**Error:**
```
NoSuchMethodException during reflection
```

**Solution:**
Add to `reflect-config.json`:
```json
{
  "name": "com.example.MyClass",
  "allDeclaredMethods": true
}
```

## Performance Metrics

### Startup Time

| Version | Startup Time |
|---------|-------------|
| JVM     | ~3-5s       |
| Native  | ~0.05-0.1s  |

### Memory Usage

| Version | RSS Memory |
|---------|-----------|
| JVM     | ~200-300MB|
| Native  | ~50-80MB  |

### Binary Size

| Version | Size      |
|---------|-----------|
| JVM     | ~15MB     |
| Native  | ~80-120MB |

## CI/CD Integration

### GitHub Actions

See `.github/workflows/native-build.yml` for complete workflow.

**Key points:**
- Uses Mandrel (GraalVM distribution)
- Container build for consistency
- Caches Maven dependencies
- Multi-platform support

### GitLab CI

```yaml
build-native:
  image: quay.io/quarkus/ubi-quarkus-mandrel-builder-image:jdk-21
  script:
    - ./mvnw clean package -Pnative -DskipTests
  artifacts:
    paths:
      - target/*-runner
```

## Best Practices

1. **Use Container Builds in CI/CD**
    - Consistent environment
    - No GraalVM setup needed

2. **Local Development**
    - Use JVM mode for faster feedback
    - Build native for testing only

3. **Memory Settings**
    - Start with 6GB
    - Increase if needed
    - Monitor build logs

4. **Testing**
    - Always test native binary
    - Run integration tests
    - Check startup time

5. **Optimization**
    - Profile with `perf` on Linux
    - Use PGO (Profile-Guided Optimization)
    - Minimize reflection usage

## References

- [Quarkus Native Guide](https://quarkus.io/guides/building-native-image)
- [GraalVM Documentation](https://www.graalvm.org/latest/docs/)
- [Mandrel Releases](https://github.com/graalvm/mandrel/releases)