<div align="center">

<table>
  <tr>
    <td>
      <img src="Images/2FAsim.png" alt="2FA" height="150" />
    </td>
    <td style="vertical-align: middle;">
      <h2 style="font-size: 0em; margin: 0;">Two Factor Authentication</h2>
    </td>
  </tr>
</table>

</div>

## Overview

2FASim is a Spring Boot web application demonstrating user authentication with optional Two-Factor Authentication (2FA) using Time-Based One-Time Passwords (TOTP). The 2FA implementation is compatible with popular authenticator apps like Google Authenticator.

### Key Features
- User registration with username, email, and password
- Optional 2FA enablement during registration
- Automatic generation of a TOTP secret and QR code for 2FA setup
- Secure login with username/password and, if enabled, TOTP code verification
- "Remember Me" functionality for user sessions
- Integration with PostgreSQL database
- Spring Security for authentication and authorization

---

## Getting Started

### Prerequisites

- Java 27+ installed
- PostgreSQL database running and accessible
- Maven or Gradle for building (optional if using pre-built jar)
- Internet access for QR code generation (uses QRServer API)

---

### Running the Application from JAR

1. **Build the JAR** (if you haven't yet):

```bash
./mvnw clean package
