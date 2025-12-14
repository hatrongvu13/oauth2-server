# oauth2-server

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/oauth2-server-1.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- Elytron Security OAuth 2.0 ([guide](https://quarkus.io/guides/security-oauth2)): Secure your applications with OAuth2
  opaque tokens
- REST Qute ([guide](https://quarkus.io/guides/qute-reference#rest_integration)): Qute integration for Quarkus REST.
  This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- REST Jackson ([guide](https://quarkus.io/guides/rest#json-serialisation)): Jackson serialization support for Quarkus
  REST. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it
- Elytron Security JDBC ([guide](https://quarkus.io/guides/security-jdbc)): Secure your applications with
  username/password stored in a database
- YAML Configuration ([guide](https://quarkus.io/guides/config-yaml)): Use YAML to configure your Quarkus application
- Elytron Security Properties File ([guide](https://quarkus.io/guides/security-properties)): Secure your applications
  using properties files
- Hibernate ORM with Panache ([guide](https://quarkus.io/guides/hibernate-orm-panache)): Simplify your persistence code
  for Hibernate ORM via the active record or the repository pattern
- SmallRye JWT ([guide](https://quarkus.io/guides/security-jwt)): Secure your applications with JSON Web Token
- SmallRye JWT Build ([guide](https://quarkus.io/guides/security-jwt-build)): Create JSON Web Token with SmallRye JWT
  Build API
- JDBC Driver - PostgreSQL ([guide](https://quarkus.io/guides/datasource)): Connect to the PostgreSQL database via JDBC

## Provided Code

### YAML Config

Configure your application with YAML

[Related guide section...](https://quarkus.io/guides/config-reference#configuration-examples)

The Quarkus application configuration is located in `src/main/resources/application.yml`.

### Hibernate ORM

Create your first JPA entity

[Related guide section...](https://quarkus.io/guides/hibernate-orm)

[Related Hibernate with Panache section...](https://quarkus.io/guides/hibernate-orm-panache)

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### REST Qute

Create your web page using Quarkus REST and Qute

[Related guide section...](https://quarkus.io/guides/qute#type-safe-templates)

### Cấu trúc dự án base

```angular2html
oauth2-server/
├── src/
│   ├── main/
│   │   ├── java/com/htv/oauth2/
│   │   │   ├── config/                    # Configuration classes
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   ├── RedisConfig.java
│   │   │   │   ├── JwtConfig.java
│   │   │   │   └── CorsConfig.java
│   │   │   │
│   │   │   ├── domain/                    # Domain models (Entities)
│   │   │   │   ├── User.java
│   │   │   │   ├── Client.java            # OAuth2 Client
│   │   │   │   ├── AuthorizationCode.java
│   │   │   │   ├── AccessToken.java
│   │   │   │   ├── RefreshToken.java
│   │   │   │   ├── Scope.java
│   │   │   │   └── AuditLog.java
│   │   │   │
│   │   │   ├── repository/                # Data Access Layer
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── ClientRepository.java
│   │   │   │   ├── TokenRepository.java
│   │   │   │   └── AuditLogRepository.java
│   │   │   │
│   │   │   ├── service/                   # Business Logic
│   │   │   │   ├── auth/
│   │   │   │   │   ├── AuthenticationService.java
│   │   │   │   │   ├── AuthorizationService.java
│   │   │   │   │   └── TokenService.java
│   │   │   │   ├── user/
│   │   │   │   │   ├── UserService.java
│   │   │   │   │   └── UserRegistrationService.java
│   │   │   │   ├── client/
│   │   │   │   │   └── ClientService.java
│   │   │   │   ├── token/
│   │   │   │   │   ├── TokenGenerationService.java
│   │   │   │   │   ├── TokenValidationService.java
│   │   │   │   │   └── TokenRevocationService.java
│   │   │   │   └── security/
│   │   │   │       ├── PasswordService.java
│   │   │   │       └── MfaService.java
│   │   │   │
│   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   │   ├── request/
│   │   │   │   │   ├── LoginRequest.java
│   │   │   │   │   ├── TokenRequest.java
│   │   │   │   │   ├── RegisterRequest.java
│   │   │   │   │   └── ClientRegistrationRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── TokenResponse.java
│   │   │   │       ├── UserResponse.java
│   │   │   │       └── ErrorResponse.java
│   │   │   │
│   │   │   ├── mapper/                    # DTO <-> Entity mappers
│   │   │   │   ├── UserMapper.java
│   │   │   │   ├── ClientMapper.java
│   │   │   │   └── TokenMapper.java
│   │   │   │
│   │   │   ├── resource/                  # REST Controllers
│   │   │   │   ├── OAuth2Resource.java    # /oauth2/*
│   │   │   │   ├── TokenResource.java     # /oauth2/token
│   │   │   │   ├── AuthorizeResource.java # /oauth2/authorize
│   │   │   │   ├── UserResource.java      # /api/users
│   │   │   │   ├── ClientResource.java    # /api/clients
│   │   │   │   └── IntrospectionResource.java # /oauth2/introspect
│   │   │   │
│   │   │   ├── security/                  # Security components
│   │   │   │   ├── filter/
│   │   │   │   │   ├── RateLimitFilter.java
│   │   │   │   │   └── AuditLogFilter.java
│   │   │   │   ├── provider/
│   │   │   │   │   └── CustomAuthenticationProvider.java
│   │   │   │   └── TokenValidator.java
│   │   │   │
│   │   │   ├── exception/                 # Exception Handling
│   │   │   │   ├── OAuth2Exception.java
│   │   │   │   ├── InvalidTokenException.java
│   │   │   │   ├── InvalidClientException.java
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   │
│   │   │   ├── cache/                     # Cache management
│   │   │   │   ├── TokenCache.java
│   │   │   │   └── UserCache.java
│   │   │   │
│   │   │   ├── scheduler/                 # Scheduled tasks
│   │   │   │   ├── TokenCleanupScheduler.java
│   │   │   │   └── AuditLogCleanupScheduler.java
│   │   │   │
│   │   │   └── util/                      # Utilities
│   │   │       ├── JwtUtil.java
│   │   │       ├── CryptoUtil.java
│   │   │       └── ValidationUtil.java
│   │   │
│   │   ├── resources/
│   │   │   ├── application.yml            # Main config
│   │   │   ├── application-dev.yml
│   │   │   ├── application-prod.yml
│   │   │   ├── db/migration/              # Flyway migrations
│   │   │   │   ├── V1__create_users_table.sql
│   │   │   │   ├── V2__create_clients_table.sql
│   │   │   │   ├── V3__create_tokens_table.sql
│   │   │   │   └── V4__create_audit_logs_table.sql
│   │   │   └── templates/                 # Qute templates
│   │   │       ├── login.html
│   │   │       ├── consent.html
│   │   │       └── error.html
│   │   │
│   │   └── docker/
│   │       └── Dockerfile.jvm
│   │
│   └── test/
│       └── java/com/htv/oauth2/
│           ├── integration/               # Integration tests
│           │   ├── OAuth2FlowTest.java
│           │   └── TokenEndpointTest.java
│           ├── service/                   # Service tests
│           │   └── TokenServiceTest.java
│           └── resource/                  # API tests
│               └── OAuth2ResourceTest.java
│
├── .github/workflows/
│   ├── ci.yml                             # CI/CD pipeline
│   └── security-scan.yml
│
├── k8s/                                   # Kubernetes manifests
│   ├── deployment.yaml
│   ├── service.yaml
│   ├── ingress.yaml
│   ├── configmap.yaml
│   └── secret.yaml
│
├── docker-compose.yml                     # Local development
├── README.md
└── pom.xml
```
## Cấu hình TokenProvider
| Hạng mục                       | Công nghệ / Tiêu chuẩn (2025)                       | Lý do chọn (theo NIST SP 800-63B, RFC 9449, OWASP ASVS 2025)                            |
| ------------------------------ | --------------------------------------------------- | --------------------------------------------------------------------------------------- |
| **Thuật toán ký**              | **Ed25519** (ưu tiên #1) <br> RS256 (hậu bị #2)     | Ed25519 nhanh gấp 10 lần RSA, độ an toàn tương đương, chống tấn công lượng tử ở mức tốt |
| **Thuật toán mã hoá key**      | RSA-OAEP-256 <br> hoặc ECDH-ES + A256GCM            | Chống tấn công lượng tử tốt hơn RSA-PKCS1, hỗ trợ Perfect Forward Secrecy               |
| **Refresh Token**              | PASETO v4.local (ưu tiên) <br> hoặc JWT + JWE       | PASETO an toàn hơn JWT (không có “alg:none”), dễ audit                                  |
| **Key Management**             | Vault <br> hoặc In-Memory + Rotate                  | Không lưu plaintext key, hỗ trợ xoay vòng định kỳ                                       |
| **Signing Key**                | Ed25519 (ưu tiên) <br> hoặc RSA-4096                | Cả hai đều đạt mức bảo mật 128-bit theo NIST                                            |
| **Key ID**                     | `kid` + JWK                                         | Hỗ trợ key rotation và revocation                                                       |
| **Token Format**               | JWS (ưu tiên) <br> hoặc JWE                         | JWE dùng khi cần mã hóa nội dung token                                                  |
| **Token Expiration**           | Access: **15 phút** <br> Refresh: **30 ngày**       | Giảm rủi ro lộ token + tuân chuẩn NIST 800-63B (short-lived credential)                 |
| **Token Revocation**           | Revocation list hoặc JTI + DB                       | Thu hồi token hiệu quả, chống replay                                                    |
| **Token Introspection**        | JWT (ưu tiên) <br> hoặc Introspection API           | Stateless nhanh, vẫn hỗ trợ revoke khi dùng JTI                                         |
| **Token Signing**              | JWS (ưu tiên) <br> hoặc JWE                         | JWE dùng khi cần mã hóa payload                                                         |
| **Token Storage**              | HttpOnly + Secure + SameSite=Strict + Signed cookie | Chống XSS, CSRF, cookie tampering                                                       |
| **Token Transmission**         | HTTPS only (TLS 1.3)                                | Bảo mật kênh truyền theo chuẩn OWASP 2025                                               |
| **Token Revocation (lặp lại)** | Blacklist hoặc JTI                                  | Đồng bộ thu hồi token trên nhiều service                                                |

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│    (REST API - Resource classes)        │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│         Application Layer               │
│     (Service classes + Use Cases)       │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│          Domain Layer                   │
│    (Entities + Business Rules)          │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│      Infrastructure Layer               │
│  (Repository + Database + Redis)        │
└─────────────────────────────────────────┘
```


---- 

# OAuth2 Authorization Server

Production-ready OAuth2 Authorization Server built with Quarkus, implementing RFC 6749 (OAuth 2.0) and RFC 7636 (PKCE).

## 🎯 Features

### Core OAuth2 Features
- ✅ **Authorization Code Grant** with PKCE support
- ✅ **Refresh Token Grant**
- ✅ **Password Grant** (Resource Owner Password Credentials)
- ✅ **Client Credentials Grant** (planned)
- ✅ **Token Introspection** (RFC 7662)
- ✅ **Token Revocation** (RFC 7009)

### Security Features
- ✅ JWT-based Access Tokens
- ✅ BCrypt Password Hashing
- ✅ Multi-Factor Authentication (MFA) with TOTP
- ✅ Account Lockout after Failed Login Attempts
- ✅ Rate Limiting on Token and Login Endpoints
- ✅ PKCE (Proof Key for Code Exchange)
- ✅ User Consent Management

### Additional Features
- ✅ Redis-based Token & Session Cache
- ✅ Comprehensive Audit Logging
- ✅ Health Checks & Metrics (Prometheus)
- ✅ OpenAPI/Swagger Documentation
- ✅ Flyway Database Migrations
- ✅ Kubernetes Ready with Helm Charts
- ✅ Docker Compose for Local Development

## 🏗️ Architecture

```
┌─────────────────────────────────────────┐
│         REST API Layer                  │
│  (Resources/Controllers)                │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│      Service Layer                      │
│  - AuthorizationService                 │
│  - TokenService                         │
│  - UserService                          │
│  - ClientService                        │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│      Repository Layer                   │
│  (Panache Repositories)                 │
└────────────────┬────────────────────────┘
                 │
┌────────────────▼────────────────────────┐
│      Database                           │
│  (PostgreSQL)                           │
└─────────────────────────────────────────┘
```

## 📋 Prerequisites

- Java 21+
- Maven 3.9+
- Docker & Docker Compose (for local development)
- PostgreSQL 16+ (or use Docker Compose)
- Redis 7+ (or use Docker Compose)

## 🚀 Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/yourorg/oauth2-server.git
cd oauth2-server
```

### 2. Start Dependencies (Docker Compose)

```bash
docker-compose up -d postgres redis
```

This starts:
- PostgreSQL on port `5432`
- Redis on port `6379`

### 3. Run Application

#### Development Mode (with live reload)
```bash
./mvnw quarkus:dev
```

#### Production Mode
```bash
./mvnw clean package
java -jar target/quarkus-app/quarkus-run.jar
```

#### Native Image
```bash
./mvnw package -Pnative
./target/oauth2-server-1.0.0-runner
```

### 4. Access Endpoints

- **OAuth2 Authorization**: http://localhost:8080/oauth2/authorize
- **OAuth2 Token**: http://localhost:8080/oauth2/token
- **Swagger UI**: http://localhost:8080/swagger-ui
- **Health Check**: http://localhost:8080/q/health
- **Metrics**: http://localhost:8080/q/metrics

## 📖 API Documentation

### 1. Register User

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "SecurePass@123",
    "confirmPassword": "SecurePass@123",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### 2. Register OAuth2 Client

```bash
curl -X POST http://localhost:8080/api/clients \
  -H "Content-Type: application/json" \
  -d '{
    "clientName": "My Application",
    "description": "Sample OAuth2 client",
    "redirectUris": ["http://localhost:3000/callback"],
    "grantTypes": ["authorization_code", "refresh_token"],
    "scopes": ["read", "write"],
    "accessTokenValidity": 3600,
    "refreshTokenValidity": 86400
  }'
```

Response includes `client_id` and `client_secret` (save these!).

### 3. Authorization Code Flow

#### Step 1: Get Authorization Code

Open in browser:
```
http://localhost:8080/oauth2/authorize?
  response_type=code&
  client_id=YOUR_CLIENT_ID&
  redirect_uri=http://localhost:3000/callback&
  scope=read write&
  state=random_state&
  code_challenge=CHALLENGE&
  code_challenge_method=S256
```

User logs in and approves. Redirects to:
```
http://localhost:3000/callback?code=AUTH_CODE&state=random_state
```

#### Step 2: Exchange Code for Tokens

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=authorization_code" \
  -d "code=AUTH_CODE" \
  -d "redirect_uri=http://localhost:3000/callback" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET" \
  -d "code_verifier=VERIFIER"
```

Response:
```json
{
  "access_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "abc123def456...",
  "scope": "read write"
}
```

### 4. Refresh Token

```bash
curl -X POST http://localhost:8080/oauth2/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=refresh_token" \
  -d "refresh_token=YOUR_REFRESH_TOKEN" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET"
```

### 5. Token Introspection

```bash
curl -X POST http://localhost:8080/oauth2/introspect \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=YOUR_ACCESS_TOKEN" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET"
```

Response:
```json
{
  "active": true,
  "scope": "read write",
  "client_id": "your_client_id",
  "username": "john_doe",
  "token_type": "Bearer",
  "exp": 1735689600,
  "iat": 1735686000,
  "sub": "user-id-123"
}
```

### 6. Revoke Token

```bash
curl -X POST http://localhost:8080/oauth2/revoke \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "token=YOUR_TOKEN" \
  -d "client_id=YOUR_CLIENT_ID" \
  -d "client_secret=YOUR_CLIENT_SECRET"
```

## 🔧 Configuration

### Environment Variables

```bash
# Database
DB_URL=jdbc:postgresql://localhost:5432/oauth2db
DB_USERNAME=oauth2user
DB_PASSWORD=oauth2pass

# Redis
REDIS_URL=redis://localhost:6379

# JWT
JWT_ISSUER=https://oauth2.example.com
ACCESS_TOKEN_EXPIRY=3600
REFRESH_TOKEN_EXPIRY=86400

# Rate Limiting
RATE_LIMIT_LOGIN_MAX=5
RATE_LIMIT_TOKEN_MAX=100

# CORS
CORS_ORIGINS=https://app.example.com
```

### application.yml Profiles

- **dev**: Development with debug logging
- **test**: Testing with in-memory database
- **prod**: Production with optimized settings

## 🐳 Docker Deployment

### Build Image

```bash
docker build -f src/main/docker/Dockerfile.jvm -t oauth2-server:1.0.0 .
```

### Run with Docker Compose

```bash
docker-compose up -d
```

This starts:
- PostgreSQL
- Redis
- OAuth2 Server
- pgAdmin (optional, with `--profile tools`)
- Redis Commander (optional, with `--profile tools`)

## ☸️ Kubernetes Deployment

### Apply Manifests

```bash
# Create namespace
kubectl create namespace oauth2

# Apply secrets and configmaps
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/configmap.yaml

# Deploy application
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/ingress.yaml

# Enable autoscaling
kubectl apply -f k8s/hpa.yaml
```

### Verify Deployment

```bash
kubectl get pods -n oauth2
kubectl logs -f deployment/oauth2-server -n oauth2
kubectl get svc -n oauth2
```

## 📊 Monitoring

### Health Checks

```bash
# Liveness
curl http://localhost:8080/q/health/live

# Readiness
curl http://localhost:8080/q/health/ready

# Full health
curl http://localhost:8080/q/health
```

### Metrics (Prometheus)

```bash
curl http://localhost:8080/q/metrics
```

Key metrics:
- `oauth2_token_issued_total` - Total tokens issued
- `oauth2_token_revoked_total` - Total tokens revoked
- `oauth2_login_attempts_total` - Total login attempts
- `oauth2_authorization_code_issued_total` - Total auth codes issued

## 🧪 Testing

### Run All Tests

```bash
./mvnw test
```

### Run Integration Tests

```bash
./mvnw verify
```

### Run Specific Test

```bash
./mvnw test -Dtest=UserServiceTest
```

## 🔒 Security Best Practices

1. **Never commit secrets** - Use environment variables or secret management
2. **Enable HTTPS in production** - Update JWT issuer to HTTPS URL
3. **Rotate client secrets regularly**
4. **Monitor audit logs** for suspicious activity
5. **Enable rate limiting** to prevent brute force attacks
6. **Use PKCE** for mobile and SPA clients
7. **Implement MFA** for sensitive operations
8. **Keep dependencies updated** - Run `./mvnw versions:display-dependency-updates`

## 📝 Database Schema

### Main Tables
- `users` - User accounts
- `oauth2_clients` - OAuth2 clients
- `authorization_codes` - Short-lived auth codes
- `access_tokens` - JWT access tokens (metadata)
- `refresh_tokens` - Long-lived refresh tokens
- `audit_logs` - Audit trail
- `user_consents` - User consent records

### Relationships
```
users ─┬─ authorization_codes
       ├─ access_tokens
       ├─ refresh_tokens
       ├─ audit_logs
       └─ user_consents

oauth2_clients ─┬─ authorization_codes
                └─ user_consents
```

## 🛠️ Development

### Project Structure

```
src/
├── main/
│   ├── java/com/htv/oauth2/
│   │   ├── config/          # Configuration
│   │   ├── domain/          # Entities
│   │   ├── dto/             # Data Transfer Objects
│   │   ├── exception/       # Custom exceptions
│   │   ├── mapper/          # MapStruct mappers
│   │   ├── repository/      # Panache repositories
│   │   ├── resource/        # REST controllers
│   │   ├── scheduler/       # Scheduled tasks
│   │   ├── service/         # Business logic
│   │   └── util/            # Utilities
│   └── resources/
│       ├── application.yml  # Configuration
│       └── db/migration/    # Flyway migrations
└── test/                    # Tests
```

```angular2html

```

### Adding New Features

1. Create entity in `domain/`
2. Create repository in `repository/`
3. Create service in `service/`
4. Create DTOs in `dto/`
5. Create mapper in `mapper/`
6. Create resource in `resource/`
7. Write tests in `test/`

## 🤝 Contributing

1. Fork the repository
2. Create feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open Pull Request

## 📄 License

This project is licensed under the MIT License - see LICENSE file for details.

## 🙋 Support

- **Documentation**: https://docs.example.com
- **Issues**: https://github.com/yourorg/oauth2-server/issues
- **Email**: support@example.com

## 🎓 References

- [OAuth 2.0 RFC 6749](https://tools.ietf.org/html/rfc6749)
- [PKCE RFC 7636](https://tools.ietf.org/html/rfc7636)
- [Token Introspection RFC 7662](https://tools.ietf.org/html/rfc7662)
- [Token Revocation RFC 7009](https://tools.ietf.org/html/rfc7009)
- [Quarkus Documentation](https://quarkus.io/guides/)