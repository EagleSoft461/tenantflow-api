[![GitHub Release](https://img.shields.io/badge/Release-v0.2.0-green.svg)](https://github.com/your-github-username/tenantflow-api/releases)
[![Java Version](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Docker](https://img.shields.io/badge/Docker-Containerized-blue.svg)](https://www.docker.com/)

# TenantFlow - Distributed Multi-Tenant B2B SaaS Infrastructure

TenantFlow is an enterprise-grade, high-performance **B2B SaaS (Software as a Service)** backend infrastructure designed around a **Single-Database, Discriminator-Based (Shared Schema)** multi-tenancy model.

The architecture guarantees strict, thread-safe logical data isolation, decentralized token-driven tenant identification, and automated query scoping to mitigate cross-tenant data leaks or horizontal privilege escalation.

---

## 🏗️ System Architecture Diagram

Below is the request-response lifecycle and logical context propagation of TenantFlow:
```
[Inbound HTTP Request]
│
▼
┌───────────────┐
│ Spring Web    │ ◄── Permitted anonymous routes bypass token checks
│ Filter Chain  │     (e.g., /api/v1/auth/login)
└───────┬───────┘
│
▼
┌───────────────┐
│  JwtFilter    │ ──► Parses Authorization: Bearer 
└───────┬───────┘     Extracts claims: sub (Email), tenantId, role
│
▼
┌───────────────┐
│TenantIntercept│ ──► Grabs tenantId from Context / Header
└───────┬───────┘     Invokes TenantContext.setCurrentTenant(tenantId)
│
▼
┌───────────────┐
│ ThreadLocal   │ ◄── Allocates safe runtime storage isolated
│ Runtime Bound │     per concurrent asynchronous worker thread
└───────┬───────┘
│
▼
┌───────────────┐
│Service Layer  │ ──► Dynamically injects tenantId to data payloads
└───────┬───────┘     Strips business logic from multi-tenant plumbing
│
▼
┌───────────────┐
│  Data Layer   │ ──► Hibernate 6.6.5 ORM / Spring Data JPA
│ (PostgreSQL)  │     Automatically scopes queries via Tenant Discriminator
└───────────────┘     e.g., SELECT * FROM users WHERE tenant_id = ?
```

---

## 🛠️ Production Tech Stack

* **Core Runtime:** Java 17 (LTS) / Spring Boot 3.4.2
* **Persistence Engineering:** Spring Data JPA / Hibernate 6.6.5 (Core Engine)
* **Security & Token Cryptography:** Spring Security 6.x / JJWT (Java JWT Api) 0.12.5
* **Database & Infrastructure:** PostgreSQL 15 Engine / Containerized via Docker Compose
* **Connection Pooling:** HikariCP (Optimized low-latency production connection management)

---

## 🧠 Core Architectural Pillars & Micro-Implementations

### 1. Decentralized Tenant Identification via Signed JWT Payload
Instead of forcing edge routers or frontend clients to statefully track company boundaries, the system uses a **Stateless Token Micro-Architecture**. Upon explicit verification via `AuthService`, business entity structures are combined with user claims and signed with an **HMAC-SHA Cryptographic Key**, packing the `tenantId` into the claims body.

### 2. Thread-Safe Context Propagation (`ThreadLocal`)
To prevent concurrent race conditions or cross-context memory mutation in high-throughput asynchronous environments, the backend binds the intercepted state to a custom thread-scoped execution wrapper (`TenantContext`). This memory zone is garbage-collected cleanly at the completion of each HTTP dispatch request-response cycle.

### 3. Discriminator-Scoped Data Isolation Barrier
Data partitioning is enforced deterministically at the database interface. Spring Data JPA repository layers query matching boundaries using automated method specifications (`findByTenantId`). This guarantees that multi-tenant safety structures are abstracted away from product features, shielding company data blocks from foreign entities.

---

## 📊 End-to-End Integration Verification (PowerShell)

### Pipeline 1: User Verification & Token Issuance
Authenticates credentials against the isolated tenant database and returns a structurally bound JWT.
```powershell
Invoke-RestMethod -Method Post -Uri "http://localhost:8081/api/v1/auth/login" `
  -ContentType "application/json; charset=utf-8" `
  -Body '{"email":"mehmet@berberali.com","password":"123"}'
```

---

## Pipeline 2: Context-Aware Isolated Data Query
Executes logical runtime routing. The backend processes data blocks exclusively matched to the client's workspace boundaries.
````powershell
Invoke-RestMethod -Uri "http://localhost:8081/api/v1/test/users" -Headers @{"X-Tenant-ID"="berber-ali"}
````
---

## 📈 Roadmap & Production Roadmap Extensions
- [ ] **Dynamic Multi-Tenant Schema Routing:** Upgrade to a shared-database, discrete physical schema structure using Hibernate MultiTenantConnectionProvider.
- [ ] **Asymmetric Cryptography:** Transition token signatures from HMAC symmetric keys to secure RS256 private/public key pairs.
- [ ] **Crypto-Hashing:** Integrate BCrypt standard password hashing algorithms into the user registration pipeline.

---

## 🤝 Contributing

Contributions, issues, and feature requests are welcome! This infrastructure is designed to be a scalable blueprint for modern SaaS developers. Feel free to check the [issues page](https://github.com/EagleSoft461/tenantflow-api/issues) if you want to contribute to the core middleware architecture.

---

## 📜 License

Distributed under the **MIT License**. This means you are completely free to use, modify, and distribute this multi-tenant architecture for both personal and commercial SaaS production applications. See `LICENSE` for more information.

---

## ✉️ Contact & Author

**Alior** - Backend & Infrastructure Engineer
* **GitHub:** [@EagleSoft461](https://github.com/EagleSoft461)
* **LinkedIn:** [ALI ORHAN OK](https://www.linkedin.com/in/ali-orhan-ok-309a2a38a/)

Project Link: [https://github.com/EagleSoft461/tenantflow-api](https://github.com/EagleSoft461/tenantflow-api)

---
*Maintained with 💻 and 🧠 for high-concurrency SaaS scalability.*
