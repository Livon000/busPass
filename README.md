# KTU Bus Pass Portal

A server-rendered bus pass website for KTU students. Students sign in with their **KTU register number** and password, apply for a monthly or semester bus pass, and carry a scannable QR pass. The bus pass cell approves or rejects applications from an admin dashboard.

Built with **Java 17 + Spring Boot 3.5 + Thymeleaf + Spring Security + Spring Data JPA + H2**. All application logic is Java; pages are rendered on the server with Thymeleaf, so there is no JavaScript framework or separate frontend build.

## Features

- Login with KTU register number (e.g. `TVE22CS001`) and BCrypt-hashed password
- Student self-registration with KTU register-number format validation
- Role-based access: `STUDENT` and `ADMIN`
- Bus pass application: route, monthly/semester duration, start date, fee calculation (semester = 6 months with 10% off)
- One open pass per student; pending applications can be cancelled
- Admin dashboard with status counts, filters, approve/reject and remarks
- Printable pass with a QR code that links to a public verification page
- Password change, CSRF protection, remember-me, custom 403/404 pages
- Demo data: five Kerala routes, three students and one admin seeded on first run
- Integration tests for authentication, authorization, registration and page rendering

## Run it

Requires Java 17+ (no separate Maven install needed — the Maven wrapper is included).

```bash
cd ~/Desktop/buspass
./mvnw spring-boot:run
```

Open <http://localhost:8080>. The database is created at `data/buspass.mv.db` and survives restarts. Delete the `data/` folder to reseed.

### Demo accounts

| Role | Register no | Password |
| --- | --- | --- |
| Student (active pass) | `TVE22CS001` | `Passw0rd!` |
| Student (pending pass) | `TVE22EC014` | `Passw0rd!` |
| Student (no pass) | `TVE21ME007` | `Passw0rd!` |
| Admin (bus pass cell) | `KTUADMIN` | `Admin@123` |

The login page has a collapsible "Demo credentials" panel. Remove it and the seeded accounts before any real deployment.

### Seeded routes

| Code | Route | Monthly fare |
| --- | --- | --- |
| R1 | Kazhakkoottam → KTU Campus (Technopark) | ₹700 |
| R2 | East Fort → KTU Campus | ₹1,100 |
| R3 | Kollam → KTU Campus | ₹2,400 |
| R4 | Neyyattinkara → KTU Campus | ₹1,600 |
| R5 | Attingal → KTU Campus | ₹1,800 |

## How the main flow works

1. A student registers with a register number matching `^[A-Za-z]{3}[0-9]{2}[A-Za-z]{2}[0-9]{3}$` (3-letter college code, 2-digit admission year, 2-letter branch, 3-digit roll).
2. Spring Security authenticates with the register number as the username. Passwords are stored as BCrypt hashes.
3. The student applies for a route and duration. The server computes the fee and validity dates and stores the application as `PENDING`.
4. An admin approves it. The pass becomes `APPROVED` and its QR code points to `/verify/{reference}`.
5. Anyone (for example a conductor) can open the verification page to see whether the pass is active today.

## Project layout

```
src/main/java/in/edu/ktu/buspass/
  config/      SecurityConfig, DataSeeder
  domain/      Student, BusRoute, PassApplication, enums
  repository/  Spring Data JPA repositories
  service/     StudentService, PassService, QrCodeService
  web/         Controllers, DTOs, global advice and error handling
src/main/resources/
  templates/   Thymeleaf pages (login, dashboard, apply, pass, admin, verify, errors)
  static/      CSS and favicon
src/test/      MockMvc integration tests
```

## Configuration

`src/main/resources/application.properties`:

- `spring.datasource.url` — H2 file database (switch to PostgreSQL/MySQL for production)
- `spring.h2.console.enabled=false` — set to `true` only for local development at `/h2-console`
- `server.forward-headers-strategy=framework` — makes redirects and pass QR links use the public HTTPS URL when running behind a proxy or tunnel

Pass QR codes are generated from the incoming request URL, so they work on localhost and on a published domain without extra configuration.

## Tests

```bash
./mvnw test
```

14 tests cover the public login page, register-number validation, successful and failed sign-in, student/admin authorization, dashboard/pending state, pass ownership, application redirects and pass verification.

## Before deploying

- Change the remember-me key in `SecurityConfig` and move secrets to environment variables.
- Disable the H2 console and use a production database.
- Remove the seeded demo accounts and the demo-credentials panel.
- Serve over HTTPS, add rate limiting on `/verify/**` and consider masking holder details on the public page.
- Payment is intentionally out of scope: the flow records the fee and assumes payment at the counter.
