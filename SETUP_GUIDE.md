# OAuth2 Server - Complete Setup Guide

## 📦 What Has Been Fixed

### 1. ✅ Removed Invalid Dependency
```xml
<!-- ❌ REMOVED - Does not exist -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-rate-limiter</artifactId>
</dependency>

<!-- ✅ ADDED - Alternative -->
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.10.1</version>
</dependency>
```

### 2. ✅ Replaced MapStruct with Manual Mappers
**Reason**: MapStruct annotation processing was causing CDI injection failures in Quarkus.

**Solution**: Created manual mapper implementations that work perfectly with CDI.

### 3. ✅ Added Missing Packages

#### **config/** - Configuration classes
- `ApplicationConfig` - Application startup
- `SecurityConfig` - Security settings
- `JwtConfig` - JWT configuration
- `CorsFilter` - CORS handling
- `RedisConfig` - Redis setup
- `OAuth2Config` - OAuth2 settings
- `DatabaseConfig` - Database info
- `JacksonConfig` - JSON serialization

#### **security/** - Security components
- `filter/RateLimitFilter` - Rate limiting
- `filter/AuditLogFilter` - Audit logging
- `filter/SecurityHeadersFilter` - Security headers
- `filter/AuthenticationFilter` - JWT authentication
- `TokenValidator` - Token validation
- `OAuth2SecurityContext` - Custom security context

#### **cache/** - Caching layer
- `TokenCache` - Token caching
- `UserCache` - User caching
- `ClientCache` - Client caching
- `SessionCache` - Session management
- `RateLimitCache` - Rate limiting counters
- `AuthorizationCodeCache` - Auth code caching

## 🚀 Complete Setup Steps

### Step 1: Project Structure

Create this directory structure:

```
src/main/java/com/htv/oauth2/
├── cache/
│   ├── AuthorizationCodeCache.java
│   ├── ClientCache.java
│   ├── RateLimitCache.java
│   ├── SessionCache.java
│   ├── TokenCache.java
│   └── UserCache.java
├── config/
│   ├── ApplicationConfig.java
│   ├── CorsFilter.java
│   ├── DatabaseConfig.java
│   ├── JacksonConfig.java
│   ├── JwtConfig.java
│   ├── LoggingInterceptor.java
│   ├── OAuth2Config.java
│   ├── RedisConfig.java
│   └── SecurityConfig.java
├── domain/
│   ├── AccessToken.java
│   ├── AuditLog.java
│   ├── AuthorizationCode.java
│   ├── Client.java
│   ├── RefreshToken.java
│   ├── User.java
│   └── UserConsent.java
├── dto/
│   ├── request/
│   │   ├── AuthorizationRequest.java
│   │   ├── ClientRegistrationRequest.java
│   │   ├── ClientUpdateRequest.java
│   │   ├── ConsentRequest.java
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── TokenRequest.java
│   │   └── UserUpdateRequest.java
│   └── response/
│       ├── AuthorizationResponse.java
│       ├── AuditLogResponse.java
│       ├── ClientResponse.java
│       ├── ErrorResponse.java
│       ├── LoginResponse.java
│       ├── TokenResponse.java
│       └── UserResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── OAuth2Exception.java
│   └── [All other exception classes]
├── mapper/
│   ├── AuthorizationCodeMapper.java
│   ├── AuditLogMapper.java
│   ├── ClientMapper.java
│   ├── TokenMapper.java
│   └── UserMapper.java
├── repository/
│   ├── AccessTokenRepository.java
│   ├── AuditLogRepository.java
│   ├── AuthorizationCodeRepository.java
│   ├── ClientRepository.java
│   ├── RefreshTokenRepository.java
│   ├── UserConsentRepository.java
│   └── UserRepository.java
├── resource/
│   ├── AuthorizeResource.java
│   ├── ClientResource.java
│   ├── IntrospectionResource.java
│   ├── RevocationResource.java
│   ├── TokenResource.java
│   └── UserResource.java
├── scheduler/
│   ├── AuditLogCleanupScheduler.java
│   └── TokenCleanupScheduler.java
├── security/
│   ├── filter/
│   │   ├── AuditLogFilter.java
│   │   ├── AuthenticationFilter.java
│   │   ├── RateLimitFilter.java
│   │   └── SecurityHeadersFilter.java
│   ├── OAuth2SecurityContext.java
│   └── TokenValidator.java
├── service/
│   ├── auth/
│   │   ├── AuthenticationService.java
│   │   └── AuthorizationService.java
│   ├── client/
│   │   └── ClientService.java
│   ├── security/
│   │   └── PasswordService.java
│   ├── token/
│   │   └── TokenService.java
│   ├── user/
│   │   └── UserService.java
│   ├── AuditService.java
│   ├── CacheService.java
│   └── RateLimiterService.java
└── util/
    ├── CryptoUtil.java
    ├── DateTimeUtil.java
    ├── HttpUtil.java
    ├── JwtUtil.java
    ├── StringUtil.java
    └── ValidationUtil.java
```

### Step 2: Copy All Artifacts

Copy content from these artifacts to your project:

1. **oauth2_pom** → `pom.xml`
2. **oauth2_domain_entities** → `domain/` package
3. **oauth2_repositories** → `repository/` package
4. **oauth2_dto_request** → `dto/request/` package
5. **oauth2_dto_response** → `dto/response/` package
6. **oauth2_exceptions** → `exception/` package
7. **oauth2_exception_handler** → `exception/` package
8. **oauth2_manual_mappers** → `mapper/` package
9. **oauth2_utils** → `util/` package
10. **oauth2_services_part1** → `service/` packages
11. **oauth2_services_part2** → `service/` packages
12. **oauth2_scheduler** → `scheduler/` and `service/` packages
13. **oauth2_resources** → `resource/` package
14. **oauth2_config** → `config/` package
15. **oauth2_security_filters** → `security/` package
16. **oauth2_cache_package** → `cache/` package

### Step 3: Create Resources Directory

```
src/main/resources/
├── application.yml
├── db/migration/
│   ├── V1__create_users_table.sql
│   ├── V2__create_clients_table.sql
│   ├── V3__create_tokens_table.sql
│   ├── V4__create_audit_logs_table.sql
│   ├── V5__create_user_consents_table.sql
│   └── V6__insert_default_data.sql
└── templates/
    ├── login.html
    └── consent.html
```

Copy from:
- **oauth2_application_yml** → `application.yml`
- **oauth2_flyway_migrations** → `db/migration/*.sql`

### Step 4: Build & Run

```bash
# Clean build
./mvnw clean install

# Start dependencies
docker-compose up -d postgres redis

# Run in dev mode
./mvnw quarkus:dev
```

### Step 5: Verify Installation

```bash
# Check health
curl http://localhost:8080/q/health

# Check Swagger
open http://localhost:8080/swagger-ui

# Check metrics
curl http://localhost:8080/q/metrics
```

## 🔍 Troubleshooting

### Issue 1: CDI Injection Failed
**Symptom**: `Unsatisfied dependency for type ...Mapper`

**Solution**: Make sure you're using the manual mappers from `oauth2_manual_mappers`, not the MapStruct ones.

### Issue 2: Redis Connection Error
**Symptom**: `Could not connect to Redis`

**Solution**:
```bash
docker-compose up -d redis
# Or
docker run -d -p 6379:6379 redis:7-alpine
```

### Issue 3: Database Migration Failed
**Symptom**: `Flyway migration failed`

**Solution**:
```bash
# Reset database
docker-compose down -v
docker-compose up -d postgres
# Wait 10 seconds
./mvnw quarkus:dev
```

### Issue 4: Port Already in Use
**Symptom**: `Port 8080 is already in use`

**Solution**:
```bash
# Change port in application.yml
quarkus:
  http:
    port: 8081
```

## 🧪 Testing

### Test Default User & Client

Default credentials created by migration:

**User:**
- Username: `admin`
- Password: `Admin@123`
- Email: `admin@example.com`

**OAuth2 Client:**
- Client ID: `default-client`
- Client Secret: `default-secret`
- Redirect URI: `http://localhost:8080/callback`

### Test OAuth2 Flow

```bash
# 1. Register new user
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "email": "test@example.com",
    "password": "Test@123",
    "confirmPassword": "Test@123"
  }'

# 2. Get authorization code (open in browser)
http://localhost:8080/oauth2/authorize?response_type=code&client_id=default-client&redirect_uri=http://localhost:8080/callback&scope=read%20write&state=xyz

# 3. Exchange code for token
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=YOUR_AUTH_CODE" \
  -d "redirect_uri=http://localhost:8080/callback" \
  -d "client_id=default-client" \
  -d "client_secret=default-secret"

# 4. Introspect token
curl -X POST http://localhost:8080/oauth2/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=YOUR_ACCESS_TOKEN" \
  -d "client_id=default-client" \
  -d "client_secret=default-secret"
```

## 📊 Monitoring

### Health Checks
```bash
curl http://localhost:8080/q/health/live
curl http://localhost:8080/q/health/ready
```

### Metrics
```bash
curl http://localhost:8080/q/metrics
```

### Logs
```bash
# Follow logs
./mvnw quarkus:dev

# In another terminal
tail -f target/quarkus.log
```

## 🚢 Deployment

### Docker Build
```bash
docker build -f src/main/docker/Dockerfile.jvm -t oauth2-server:1.0.0 .
```

### Kubernetes Deploy
```bash
kubectl apply -f k8s/
```

### Environment Variables
```bash
export DB_URL=jdbc:postgresql://postgres:5432/oauth2db
export DB_USERNAME=oauth2user
export DB_PASSWORD=oauth2pass
export REDIS_URL=redis://redis:6379
export JWT_ISSUER=https://oauth2.example.com
```

## ✅ Checklist

- [ ] All files copied to correct locations
- [ ] pom.xml updated
- [ ] application.yml configured
- [ ] PostgreSQL running
- [ ] Redis running
- [ ] Build successful (`./mvnw clean install`)
- [ ] Application starts (`./mvnw quarkus:dev`)
- [ ] Health check passes
- [ ] Swagger UI accessible
- [ ] Can register user
- [ ] Can create client
- [ ] OAuth2 flow works

## 🎉 Success!

If all checklist items are complete, your OAuth2 Server is ready!

Access:
- **Application**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui
- **Health**: http://localhost:8080/q/health
- **Metrics**: http://localhost:8080/q/metrics

## 📚 Next Steps

1. Configure production database
2. Set up HTTPS/TLS
3. Configure proper JWT keys
4. Set up monitoring (Prometheus/Grafana)
5. Configure backup strategy
6. Set up CI/CD pipeline
7. Load testing
8. Security audit

## 🆘 Support

If you encounter issues:
1. Check logs: `target/quarkus.log`
2. Verify dependencies: `docker-compose ps`
3. Check database: Connect to PostgreSQL and verify tables
4. Check Redis: `redis-cli ping`
5. Review configuration: `application.yml`

Good luck! 🚀