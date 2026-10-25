# Smart Parking Management System

A full-stack web app for managing parking slots, vehicle entry/exit, automatic fee calculation and daily revenue.
Built as an individual PBL project: **React (Vite) + Spring Boot + PostgreSQL**.

## Features
- **Dashboard** – total / available / occupied slots, active bookings, occupancy %, revenue for a chosen date (all from PostgreSQL).
- **Parking slots** – responsive grid (green = available, red = occupied, shows vehicle number), add new slots. 10 sample slots (`A-01`…`A-10`) are seeded.
- **Vehicle entry** – validated form, only available slots listed, blocks occupied slots and duplicate active vehicles.
- **Active bookings** – duration, live estimated fee (`Fare` button / `Refresh`), `Checkout`, and an "Awaiting payment" table with **Mark as Paid**.
- **Checkout** – one database transaction: fixes the fee, stores exit time, completes the booking, frees the slot, shows a receipt.
- **Revenue report** – date picker; only **PAID** bookings count, filtered by payment date in `Asia/Kolkata` time.
- Global error handling (`@RestControllerAdvice`), Swagger UI, Postman collection, unit tests.

## Fee rule
`Fee = ₹50 × ceiling(duration in seconds / 10,800)` (₹0 for zero duration). Implemented once in `FeeCalculationService` and used by both the fare API and checkout. Money uses `BigDecimal`.

| Duration | Fee |
|---|---|
| 0 | ₹0 |
| 30 min / 2 h 15 m / exactly 3 h | ₹50 |
| 3 h 1 s / exactly 6 h | ₹100 |
| 6 h 10 m | ₹150 |

## Tech stack
React 18, Vite, React Router, Axios, Lucide · Java 17+, Spring Boot 3.3, Spring Data JPA, Bean Validation, Flyway · PostgreSQL · springdoc OpenAPI · JUnit 5 + Mockito · Maven.

## Prerequisites
Java 17+, Maven 3.9+, Node.js 18+, PostgreSQL 14+.

## 1. Set up PostgreSQL
Install PostgreSQL (Windows installer from postgresql.org, `brew install postgresql@16` on macOS, or `sudo apt install postgresql` on Ubuntu), then create the database:

```bash
psql -U postgres -c "CREATE DATABASE parking_db;"
```
Flyway creates the tables and the 10 sample slots automatically when the backend first starts.

## 2. Configure the backend
Spring Boot reads **environment variables** (see `backend/.env.example`). Set at least the password:

```bash
# macOS / Linux
export DB_PASSWORD='your_password_here'
# Windows PowerShell
$env:DB_PASSWORD = 'your_password_here'
```
Optional: `DB_URL`, `DB_USERNAME` (default `postgres`), `APP_TIMEZONE` (default `Asia/Kolkata`), `CORS_ALLOWED_ORIGINS` (default `http://localhost:5173`).

## 3. Run the backend
```bash
cd backend
mvn spring-boot:run
```
API: `http://localhost:8080` · Swagger UI: **http://localhost:8080/swagger-ui.html**

## 4. Run the frontend
```bash
cd frontend
cp .env.example .env      # Windows: copy .env.example .env
npm install
npm run dev
```
Open **http://localhost:5173**.

## 5. Run the tests
```bash
cd backend
mvn test
```
Covers fee boundaries, booking, checkout, slot release, payment and revenue (Mockito – no database needed).

## API endpoints
| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/slots` | All slots (with vehicle number when occupied) |
| GET | `/api/slots/available` | Available slots |
| GET | `/api/slots/{id}` | Slot by ID |
| POST | `/api/slots` | Create slot → 201 |
| GET | `/api/bookings/active` | Active bookings with duration + estimated fee |
| GET | `/api/bookings/completed?paymentStatus=PENDING` | Completed bookings (extra, powers "Awaiting payment") |
| POST | `/api/bookings` | Register vehicle entry → 201 |
| GET | `/api/bookings/{id}` | Booking details |
| GET | `/api/bookings/{id}/fare` | Current estimated fare (active bookings) |
| POST | `/api/bookings/{id}/checkout` | Checkout |
| PATCH | `/api/bookings/{id}/payment` | Mark completed booking as PAID |
| GET | `/api/reports/revenue?date=2026-10-25` | Daily revenue |
| GET | `/api/dashboard?date=2026-10-25` | Dashboard summary (extra) |

Errors return `{ timestamp, status, error, message, path }` with 400 / 404 / 409 / 500.
Import `postman/parking-api-collection.json` into Postman (run "Register vehicle entry" first).

## Project structure
```
smart-parking-system/
├── backend/   (controller, service, repository, model, dto, exception, config, db/migration, tests)
├── frontend/  (components, pages, layouts, services, hooks, utils)
├── postman/parking-api-collection.json
└── README.md
```

## Screenshots
Add your own after running the app (Dashboard, Slots, Entry, Active Bookings, Reports):
`docs/dashboard.png`, `docs/slots.png`, …

## Troubleshooting
| Problem | Fix |
|---|---|
| `password authentication failed` | `DB_PASSWORD` is wrong or not set in the same terminal that runs Maven. |
| `database "parking_db" does not exist` | Run the `CREATE DATABASE` command above. |
| `Port 8080 already in use` | `export SERVER_PORT=8081` and set `VITE_API_BASE_URL=http://localhost:8081/api` in `frontend/.env`. |
| Frontend says "Cannot reach the server" | Backend not running, or CORS origin differs: set `CORS_ALLOWED_ORIGINS`. |
| `Schema-validation: missing table` | Don't edit old migrations; add a new `V3__...sql`. For a clean start drop and recreate `parking_db`. |
| Wrong "today" in reports | Check `APP_TIMEZONE`. |

## Extra design notes (for viva)
- `parking_bookings.paid_at` (an extra column) records *when* payment happened, so revenue is "paid on that date".
- Duplicate protection is two-layered: service checks + PostgreSQL **partial unique indexes** (one ACTIVE booking per slot and per vehicle); slot rows are locked with `PESSIMISTIC_WRITE` during booking.
