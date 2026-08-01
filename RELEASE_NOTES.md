v1.1.0 — Enterprise Architecture & Security Hardening
@EagleSoft461 EagleSoft461 released this just now
 
v1.1.0

We are proud to release v1.1.0, a significant architectural leap for the TenantFlow framework! While v1.0.0 brought bulletproof integration testing and container orchestration, v1.1.0 completely refactors the application layer into a fully decoupled, secure, and enterprise-grade SaaS engine.

Güvenlik açıklarını kapattım, detaylara SECURITY.md dosyasından bakabilirsiniz.

🌟 What's New in v1.1.0
- **Data Transfer Objects (DTO) Integration**: Eradicated "Mass Assignment" vulnerabilities (Entity Exposure) by implementing strict Request and Response DTOs (`UserRegistrationRequestDTO`, `UserResponseDTO`). The presentation layer is now 100% decoupled from the database layer.
- **Advanced Request Validation**: Introduced `jakarta.validation` limits at the Controller boundaries. The API now elegantly rejects malformed payloads with structured `400 Bad Request` messages before they ever touch the business logic.
- **Centralized & Secure Error Handling**: Upgraded the `GlobalExceptionHandler` to gracefully catch and sanitize database constraints (e.g., `DataIntegrityViolationException`) and generic runtime errors, completely neutralizing internal infrastructure leakage.
- **Standardized Enterprise Logging**: Purged fragile `System.out.println()` calls and introduced robust `@Slf4j` asynchronous logging across Interceptors and Exception Handlers.
- **State Cleanup in Integration Tests**: Patched `MultiTenantIntegrationTest` with pre-flight database sanitization (`userRepository.deleteAll()`) to ensure zero-collision test cycles.

📦 Dependency & Environment Metrics
- Core Runtime: Spring Boot v3.4.2 running on Java 17
- Container & Database: PostgreSQL 15 Alpine
- Testing Infrastructure: Testcontainers JUnit 5 Extension
- Security Enhancements: DTO Pattern, Input Validation, Slf4j Logging

Maintained with 💻 and 🧠 by @EagleSoft461 for high-concurrency B2B SaaS scalability.
