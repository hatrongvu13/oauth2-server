# Security Policy

This document defines the 2025 security standards for authentication, authorization, token management, and key protection aligned with **NIST SP 800-63B**, **RFC 9449**, and **OWASP ASVS 2025**.

---

## 🔐 1. Cryptographic Standards (2025)

| Category                 | Standard                                             | Rationale                                                                              |
| ------------------------ | ---------------------------------------------------- | -------------------------------------------------------------------------------------- |
| **Signing Algorithm**    | **Ed25519** (primary), RS256 (fallback)              | Ed25519 is fast, secure, resistant to side‑channel attacks; RSA kept for compatibility |
| **Key Encryption**       | RSA‑OAEP‑256 or ECDH‑ES + A256GCM                    | PQ‑resistant approach + Perfect Forward Secrecy                                        |
| **Token Format**         | JWS (preferred), JWE (when confidentiality required) | JWS for integrity; JWE when token must be encrypted                                    |
| **Refresh Token Format** | **PASETO v4.local** (preferred) or JWT + JWE         | More secure and simpler than JWT (no `alg:none`)                                       |
| **Key Storage**          | HashiCorp Vault or in‑memory ephemeral with rotation | Prevent plaintext key exposure                                                         |
| **Signing Key Type**     | Ed25519 or RSA‑4096                                  | Both meet modern cryptographic requirements                                            |
| **Key ID**               | `kid` + JWK                                          | Enables rotation and revocation                                                        |

---

## 🔑 2. Token Lifetime Policy

* **Access Token**: 15 minutes
* **Refresh Token**: 30 days
* **Rotation**: Refresh tokens must rotate on every refresh request
* **Revocation**: Implement via blacklist or JTI table

---

## 🧰 3. Secure Token Storage

* Must be stored in **HttpOnly**, **Secure**, **SameSite=Strict** cookies
* Cookies must be **signed** (HMAC or Ed25519 detached signature)
* No localStorage/sessionStorage for sensitive credentials

---

## 🌐 4. Transport Layer Security

* **TLS 1.3 mandatory**
* HSTS enabled
* Disable TLS session resumption without tickets

---

## 🧩 5. Login / Logout / Refresh Architecture

### **Overview Diagram**

```
Client → Auth Server → Token Service → Resource APIs
```

### **5.1 Login Flow (Password / OAuth)**

1. Client sends login request (username/password or OAuth callback)
2. Auth server verifies credentials (rate‑limited, MFA optional)
3. Auth server issues:

    * **Access Token (JWS, Ed25519, 15m)**
    * **Refresh Token (PASETO v4.local, 30 days)**
4. Both tokens sent via **HttpOnly Secure cookies**

**Security Requirements:**

* Password checked with Argon2id
* Device fingerprint optional for anomaly detection
* Brute-force checks applied (RFC 9449 guidance)

---

### **5.2 Token Refresh Flow (Sliding Session)**

1. Client calls `/auth/refresh` using refresh token cookie
2. Server validates refresh token (PASETO/JWE)
3. Server checks revocation list / JTI table
4. Server issues:

    * New **access token**
    * New **refresh token** (rotated)
5. Old refresh token is added to revocation list

**Security Requirements:**

* Rotation must be atomic
* Old tokens cannot be reused
* Rate‑limit refresh endpoint

---

### **5.3 Logout Flow**

1. Client calls `/auth/logout`
2. Server:

    * Removes refresh token from DB
    * Adds token JTI to blacklist
    * Clears cookies
3. Access token expires naturally after 15 minutes

**Security Requirements:**

* Stateless access token → can't be force-invalidated directly
* Revocation list used for critical APIs

---

## 🛡 6. Key Rotation Policy

* Keys must rotate every **90 days** or immediately after compromise
* Each key must include a `kid` field
* JWK Set endpoint `/oauth/.well-known/jwks.json` must expose public keys only
* Server must retain previous keys for the duration of token TTL

---

## 🔍 7. Monitoring & Auditing

* All authentication events logged (login, refresh, logout)
* Suspicious IP / device changes flagged
* Logs must exclude plaintext secrets, tokens, or passwords

---

## 🧪 8. Testing Requirements

* Automated OWASP ZAP scanning
* JWT/JWE signature tampering tests
* Token replay detection tests
* MFA/Rate‑limit bypass attempts

---

## 📄 9. Contact

    For security issues, please report via Responsible Disclosure or Security Contact Email: hatrongvu13@gmail.com.
