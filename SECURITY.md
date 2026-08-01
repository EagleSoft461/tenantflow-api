# Security Policy

## Supported Versions

Currently, the following versions are being supported with security updates.

| Version | Supported          |
| ------- | ------------------ |
| 1.1.x   | :white_check_mark: |
| 1.0.x   | :white_check_mark: |
| < 1.0   | :x:                |

## Reporting a Vulnerability

If you discover a security vulnerability within this project, please report it privately. **Do not disclose it publicly** until it has been addressed. 

## Recent Security Hardening (v1.1.0)

In the latest release, the following architectural security vulnerabilities and structural weaknesses were closed:

### 1. Mass Assignment Vulnerability Prevention (CWE-915)
- **Issue:** The API previously accepted raw Entity objects (e.g., `User`) directly from client payloads. This could allow malicious actors to modify restricted fields (like IDs or Roles) by injecting them into the request body.
- **Fix:** Implemented the **Data Transfer Object (DTO)** pattern (`UserRegistrationRequestDTO`, `UserResponseDTO`). The API now strictly defines and limits the payload properties it accepts, fully isolating the database layer from the presentation layer.

### 2. Broken Input Validation (CWE-20)
- **Issue:** The application processed payloads without strict format validations, potentially leading to malformed data storage or backend processing errors.
- **Fix:** Integrated `jakarta.validation.constraints` (e.g., `@Email`, `@NotBlank`, `@Size`). The application now forcefully rejects malformed requests at the Controller boundaries using the `@Valid` annotation, returning sanitized `400 Bad Request` errors.

### 3. Information Exposure Through Error Messages (CWE-209)
- **Issue:** Unhandled exceptions could print stack traces or raw database constraints directly to the console or response payload, leaking infrastructure details.
- **Fix:** Enhanced the `GlobalExceptionHandler` and introduced standard `Slf4j` logging. Database errors (like `DataIntegrityViolationException`) and generic exceptions are now gracefully caught and mapped to safe, sanitized JSON responses, fully neutralizing infrastructure leakage.

*Maintained for high-concurrency and secure B2B SaaS scalability.*
