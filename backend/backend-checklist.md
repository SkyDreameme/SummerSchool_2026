Создам итеративный план реализации backend-части в виде чек-листа, оформленного в отдельном файле backend-checklist.md. План построен на основе анализа ограничений, архитектурных решений и приоритетов MoSCoW.

Backend Implementation Checklist
Project: Karting Center "Apex"
Date: 2026-07-06
Status: ⬜ Pending / ✅ Done / 🔄 In Progress / ⚠️ Blocked

📊 Progress Summary
Iteration	Tasks	✅ Done	⬜ Pending	Progress
0. Foundation	15	0	15	0%
1. Auth Service	14	0	14	0%
2. Schedule Service	12	0	12	0%
3. Booking Service	16	0	16	0%
4. Profile & Loyalty	11	0	11	0%
5. Rating Service	10	0	10	0%
6. Notification Service	8	0	8	0%
7. Admin & Monitoring	10	0	10	0%
8. Integration Tests	10	0	10	0%
9. Performance & Security	8	0	8	0%
10. Production Readiness	10	0	10	0%
    TOTAL	124	0	124	0%
    ITERATION 0: FOUNDATION (3 days)
    Repository Setup
    Initialize Go module: go mod init github.com/apex/backend

Create project structure: cmd/, internal/, pkg/, api/, scripts/

Setup .gitignore for Go, IDE, and OS files

Configure branch protection rules (main, develop)

Setup CI/CD pipeline (GitHub Actions):

go test -v -race -cover

go vet ./...

golangci-lint run

Docker & Local Development
Create docker-compose.yml with:

PostgreSQL 15-alpine

Redis 7-alpine

Backend service (hot-reload)

Create Dockerfile (multi-stage build)

Create .env.example with all required env vars

Setup Makefile with common commands:

make dev — start local environment

make migrate-up — run migrations

make seed — load test data

make test — run all tests

make build — build production binary

make lint — run linters

Configuration
Implement config package with struct and env parsing

Define all config variables:

Database: DB_HOST, DB_PORT, DB_USER, DB_PASSWORD, DB_NAME, DB_SSL_MODE

Redis: REDIS_HOST, REDIS_PORT, REDIS_PASSWORD, REDIS_DB

JWT: JWT_SECRET, JWT_EXPIRATION_HOURS

SMS: SMS_PROVIDER, SMS_API_KEY, SMS_FROM_NUMBER

FCM: FCM_SERVER_KEY

Payment: PAYMENT_BASE_URL, PAYMENT_TERMINAL_ID

Server: SERVER_PORT, SERVER_READ_TIMEOUT, SERVER_WRITE_TIMEOUT

CORS: ALLOWED_ORIGINS, ALLOWED_METHODS

Rate Limits: SMS_RATE_LIMIT, API_RATE_LIMIT

Database Migrations
Setup golang-migrate

Create migration files:

000001_init_schema.up.sql — clients, marshals, slots, bookings, ratings

000001_init_schema.down.sql

000002_add_booking_status_constraints.up.sql

000002_add_booking_status_constraints.down.sql

000003_add_slot_status_and_reason.up.sql

000003_add_slot_status_and_reason.down.sql

000004_add_loyalty_and_discount.up.sql

000004_add_loyalty_and_discount.down.sql

000005_add_booking_rated_flag.up.sql

000005_add_booking_rated_flag.down.sql

000006_add_cancellation_reason.up.sql

000006_add_cancellation_reason.down.sql

000007_add_indexes_for_performance.up.sql

000007_add_indexes_for_performance.down.sql

Implement migration runner in internal/repository/migrate.go

Repository Layer
Setup database connection pool:

Connection pooling configuration

Health check endpoint

Setup redis client:

Connection configuration

Health check endpoint

Ping on startup

Domain Models
Define domain models in internal/domain/:

client.go — Client, LoyaltyTier

slot.go — Slot, SlotStatus, TrackConfiguration

booking.go — Booking, BookingStatus

marshal.go — Marshal

rating.go — Rating

errors.go — Custom errors (ErrNoSeats, ErrBookingAlreadyExists, etc.)

Logger
Setup structured logging (slog or zap)

Add request ID to context

Configure log levels (debug, info, warn, error)

ITERATION 1: AUTH SERVICE (5 days)
SMS Integration (F-001, F-002, F-006)
Create SMS client interface (for testing)

Implement SMS provider adapter (Twilio / SMS.ru mock):

SendSMS(phone, code) method

Implement phone validation: ^\+7[0-9]{10}$ (F-006)

Create sms_attempts repository (Redis-based counter):

IncrementAttempts(phone) — increment counter

GetAttempts(phone) — get current count

Block(phone, duration) — block after 3 attempts

IsBlocked(phone) — check if blocked

Implement SMS rate limiting (F-005):

Max 3 attempts → block for 1 hour

Reset attempts on successful verification

JWT Implementation
Implement JWT generator: GenerateJWT(clientID)

Implement JWT validator: ValidateJWT(token)

Configure JWT expiration (default: 24 hours)

Add refresh token mechanism (optional)

Auth API Endpoints
POST /auth/sms — send SMS code (F-001):

Validate phone format (F-006)

Check rate limiting

Generate 4-6 digit code

Store code in Redis with TTL (5 min)

Send SMS via provider

Return success/failure

POST /auth/verify — verify SMS code (F-001):

Validate phone and code

Check code in Redis

Find or create client (F-004)

Generate JWT token

Store token in Redis (session)

Return JWT + client info

Auth Middleware
Implement JWT middleware for protected routes:

Extract JWT from Authorization header

Validate JWT signature and expiration

Check token in Redis (revocation check)

Add client info to context

Return 401 on invalid/expired token

Auth Handler (F-003)
Implement error handling:

400 — Invalid phone or code

429 — Too many attempts (F-005)

429 — SMS rate limit exceeded

500 — Internal server error

Auth Repository
Implement client repository:

FindByPhone(phone) — find client by phone

Create(client) — create new client

Update(client) — update client data

Integration Tests
TestAuthSMS — send SMS

TestAuthVerify — verify code and get JWT

TestAuthRateLimit — exceed attempts → block

TestAuthMiddleware — protected route with/without token

ITERATION 2: SCHEDULE SERVICE (5 days)
Domain Models
Implement Slot domain model with all fields:

id, marshal_id, starts_at, ends_at

track_configuration (Short/Long) (F-023)

track_description (text)

price_with_rental, price_own_gear (F-009)

total_seats, available_seats (F-010)

status (Available/CancelledByCenter) (F-016)

cancellation_reason

Slot Repository
Implement FindByDateRange(from, to):

Use indexes for performance

Return slots for 7 days (F-008)

Support custom date range (F-013)

Implement FindByID(id):

Join with marshal table

Return full slot details (F-018)

Implement LockForUpdate(tx, id) (for atomic booking)

Implement Update(tx, slot) — update available seats

Slot Service
Implement ListSlots(from, to):

Check Redis cache first (TTL: 5 min)

Query database if cache miss

Store in cache with TTL

Handle CancelledByCenter status (F-016)

Handle availableSeats < 0 → treat as 0 (F-015)

Implement GetSlot(id):

Check Redis cache

Return slot details with marshal info

No public rating (FR-18, F-012)

Implement InvalidateCache(slotID) (for updates)

Slot API Endpoints
GET /slots — list slots (F-008):

Default: 7 days from today

Query params: from, to

Return slots with availability

Filter out CancelledByCenter from list (F-016)

availableSeats <= 0 → Empty state (F-011)

GET /slots/{id} — slot details (F-018):

Return full slot info

Include marshal name

Exclude marshal rating (FR-18)

Slot Cache (Redis)
Implement cache key strategy:

slots:list:{from}:{to}

slots:detail:{id}

Implement cache invalidation:

On slot update

On booking creation (available_seats change)

Integration Tests
TestScheduleList — list slots for 7 days

TestScheduleDetail — get slot by ID

TestScheduleFilter — filter by date range

TestScheduleEmpty — no slots → empty state

TestScheduleCancelled — handle CancelledByCenter

ITERATION 3: BOOKING SERVICE (7 days)
Domain Models
Implement Booking domain model:

id, client_id, slot_id

equipment (Rental/Own) (F-025)

final_price (with discount) (F-019)

status (6 statuses) (F-035)

cancellation_reason (F-043)

rated (F-057)

deposit_link (F-030)

Loyalty System (F-050)
Implement loyalty levels:

None → 0% discount

Bronze → 10% discount

Silver → 20% discount

Implement discount calculation:

CalculateFinalPrice(basePrice, loyaltyTier)

Apply discount to final price (F-019)

Store loyalty status in clients table

Booking Repository
Implement FindActiveByClient(clientID):

Check for existing Pending or Paid booking

Used for NFR-2 validation

Implement Create(tx, booking):

Insert new booking

Return booking with generated ID

Implement FindByID(id):

Join with slot and client

Implement FindByClient(clientID):

Return all bookings for client

Support status filtering

Implement UpdateStatus(id, status, reason)

Implement SetRated(id) — mark as rated

Atomic Booking Service (F-026)
Implement CreateBooking(req) with transaction:

BEGIN TRANSACTION

Lock slot: SELECT FOR UPDATE (NFR-1)

Check availableSeats > 0 (F-027)

Check existing active booking (NFR-2) (F-028)

Calculate final price with loyalty (F-019)

Create booking with status Pending (F-035)

Decrement availableSeats

Update slot

Generate depositLink (F-030)

Set TTL in Redis (30 min) (F-045)

COMMIT

Return booking with deposit link

Booking Cancellation Service (F-037, F-040)
Implement CancelBooking(id):

Check if cancellation is allowed (F-040)

now + 2h <= startsAt (inclusive)

Validate status (Pending or Paid only)

Validate reason is provided

Update booking status → CancelledByClient

Increment availableSeats in slot

Return success with message

Booking API Endpoints
POST /bookings — create booking (F-026):

Accept: slot_id, equipment, client_id

Return: booking details + depositLink

Error: 409 NO_SEATS (F-027)

Error: 409 BOOKING_ALREADY_EXISTS (F-028)

Error: 410 SLOT_CANCELLED (F-029)

Error: 422 VALIDATION_ERROR

GET /bookings — list bookings:

Filter by status group (active/completed/cancelled) (F-047)

Return paginated results

Include slot details

GET /bookings/{id} — booking details:

Return full booking info

Include slot + marshal info

Include canCancel flag (F-037)

Include rated flag (F-057)

Hide rating if now < endsAt (F-059)

POST /bookings/{id}/cancel — cancel booking (F-037):

Request: reason (optional for client)

Error: 422 CANCEL_WINDOW_CLOSED (F-039)

Error: 422 INVALID_STATUS

Return: cancellation confirmation

TTL Processing (F-045)
Implement background worker:

Check pending_bookings_ttl table every minute

Find expired bookings (now >= expires_at)

Update status → Expired

Increment availableSeats

Mark TTL record as processed

Integration Tests
TestBookingCreate — happy path

TestBookingNoSeats — 409 NO_SEATS

TestBookingAlreadyExists — 409 BOOKING_ALREADY_EXISTS

TestBookingSlotCancelled — 410 SLOT_CANCELLED

TestBookingCancel — happy path

TestBookingCancelWindow — 422 CANCEL_WINDOW_CLOSED

TestBookingTTL — expired after 30 min

ITERATION 4: PROFILE & LOYALTY (4 days)
Profile Repository
Implement FindByID(id) — get client profile

Implement GetLoyaltyStatus(clientID) — get current loyalty tier

Implement UpdateLoyaltyStatus(clientID, status) — update tier

Profile Service
Implement GetProfile(clientID):

Return: phone, loyalty status, discount (F-050)

Include contacts (F-052)

No conditions for next level (F-053 — Won't)

Profile API Endpoints
GET /profile — get profile (F-050):

Return client data

Include loyalty status and discount

Include support contacts (F-052)

GET /profile/loyalty — get loyalty status only:

Return tier and discount

Used for caching on client side

POST /profile/logout — logout (F-051):

Invalidate JWT token in Redis

Return success

Payment Link Generation (F-030)
Implement GenerateDepositLink(bookingID):

Generate unique link with booking ID

Use external payment provider

Return URL for SBP/bank

Store in deposit_link field

Integration Tests
TestProfileGet — get profile

TestProfileLoyalty — get loyalty status

TestProfileLogout — invalidate token

ITERATION 5: RATING SERVICE (4 days)
Rating Repository
Implement Create(rating) — create rating record:

Check uniqueness (one rating per booking)

Store: booking_id, marshal_id, client_id, stars

Implement FindByBooking(bookingID) — check if already rated

Rating Service (F-055, F-056, F-057, F-059)
Implement SubmitRating(req):

Validate stars (1-5) (F-055)

Check if booking exists and is completed (F-059)

Check if already rated (F-057)

Validate rating is allowed (F-062)

Create rating record

Update rated = true in booking

Return success

Rating API Endpoints
POST /ratings — submit rating (F-055, F-060):

Request: booking_id, stars (1-5)

Error: 409 ALREADY_RATED (F-061)

Error: 422 RATING_NOT_ALLOWED (F-062)

Error: 404 BOOKING_NOT_FOUND

Return: success with message (F-060)

Push Scheduling (F-054, F-058)
Implement ScheduleRatingPush(bookingID):

Calculate time: endsAt + 1 hour

Store scheduled push in queue

Skip if booking status is CancelledByCenter (F-058)

Add to push_notifications table

Integration Tests
TestRatingSubmit — happy path (5 stars)

TestRatingAlreadyRated — 409 ALREADY_RATED

TestRatingNotAllowed — 422 RATING_NOT_ALLOWED

TestRatingCancelled — skip push if cancelled

TestRatingBeforeEnd — cannot rate before endsAt

ITERATION 6: NOTIFICATION SERVICE (3 days)
FCM Integration
Setup Firebase Cloud Messaging client

Implement SendPush(token, title, body, data):

Send notification via FCM API

Return success/failure

Log delivery status

Push Scheduler Worker
Implement background worker:

Query push_notifications with status = 'Pending' and sent_at <= NOW()

Send push via FCM (FR-13)

Update status: Sent or Failed

Store error message on failure

Retry mechanism (3 attempts)

Notification Service
Implement ScheduleNotification(bookingID, type, data):

Create push notification record

Send after 1 hour (F-054)

No action buttons (OOS-011)

Only informational (FR-13)

Push API Endpoints
POST /notifications/token — register device token:

Store FCM token for client

Client must send on login/refresh

Integration Tests
TestPushSchedule — schedule rating push

TestPushSend — send notification (mock FCM)

TestPushRetry — retry on failure

TestPushCancelled — skip push if booking cancelled

ITERATION 7: ADMIN & MONITORING (4 days)
Admin Repository
Implement CancelSlotByAdmin(slotID, reason) (F-016):

Update slot status → CancelledByCenter

Store cancellation reason

Set availableSeats = 0

Cancel all affected bookings (F-043)

Notify clients via push (F-058)

Admin API Endpoints (Internal)
POST /admin/slots/{id}/cancel — cancel slot:

Request: reason

Return: affected bookings count

Monitoring & Health
Implement /health endpoint:

Check database connection

Check Redis connection

Check external dependencies (SMS, FCM)

Return status: ok or error

Implement /metrics endpoint (Prometheus):

Request count by endpoint

Request duration (p50, p95, p99)

Error rate

Active bookings count

Available seats per slot

Error Logging & Fallbacks
Implement fallback logging (F-024, F-044):

Unknown loyalty status → log + treat as None

Unknown booking status → log + treat as Cancelled

availableSeats < 0 → log + treat as 0 (F-015)

Integration Tests
TestAdminCancelSlot — cancel slot by admin

TestHealthCheck — endpoint returns 200

TestPrometheusMetrics — metrics exposed

ITERATION 8: INTEGRATION TESTS (4 days)
API Integration Tests
Test full auth flow: SMS → Verify → JWT

Test schedule flow: List → Detail

Test booking flow: Create → Get → Cancel

Test rating flow: Push → Rate → Success

Test profile flow: Get → Logout

End-to-End Scenarios
Scenario 1: New user registration:

Send SMS → Verify → Create booking → Pay deposit (mock)

Scenario 2: Cancel booking:

Create booking → Cancel (2h before start) → Confirm

Scenario 3: Rating flow:

Booking completed → Push sent → Submit rating → Success

Scenario 4: Error scenarios:

No seats → 409 NO_SEATS

Already booked → 409 BOOKING_ALREADY_EXISTS

Cancel after window → 422 CANCEL_WINDOW_CLOSED

Database Seeding
Create seed files:

001_clients.sql — 10 clients

002_marshals.sql — 5 marshals

003_slots.sql — 20 slots

004_bookings.sql — 15 bookings

005_ratings.sql — 7 ratings

006_sms_attempts.sql — 3 attempts

007_jwt_tokens.sql — 4 tokens

Implement Makefile seed command

Test Coverage
Ensure code coverage ≥ 80%

Generate coverage report: go test -cover -coverprofile=coverage.out ./...

Check coverage thresholds in CI/CD

ITERATION 9: PERFORMANCE & SECURITY (4 days)
Performance Optimization
Optimize database queries:

Add missing indexes (000007 migration)

Use EXPLAIN to analyze slow queries

Use prepared statements for all queries

Implement Redis caching:

Slot list cache (F-008)

Slot detail cache (F-018)

Profile cache

Cache invalidation on updates

Implement pagination for /bookings endpoint

Use connection pooling for DB and Redis

Implement request timeout (10s) (F-065)

Security Hardening
Implement CORS configuration:

Allow only trusted origins

Allow methods: GET, POST, PUT, DELETE

Implement rate limiting:

Global: 100 requests/second per IP

Auth SMS: 3 attempts/1 hour (F-005)

Implement SQL injection prevention:

Use parameterized queries

Use sqlc/ent for type safety

Implement XSS protection:

Escape user input

Set appropriate Content-Security-Policy headers

Implement JWT security:

Secure token storage (Redis)

Token blacklisting on logout (F-051)

Short expiration time (24h)

Implement input validation:

Phone format: ^\+7[0-9]{10}$ (F-006)

Stars: 1-5 (F-055)

Equipment: Rental/Own (F-025)

Load Testing
Test API under load (200 RPS):

/slots — 100 RPS

/bookings — 50 RPS

/auth/sms — 30 RPS

/ratings — 20 RPS

Measure response times (p95 < 200ms)

Identify bottlenecks and optimize

ITERATION 10: PRODUCTION READINESS (3 days)
Deployment Preparation
Create production Dockerfile:

Multi-stage build

Minimal base image (scratch/alpine)

Add only necessary files

Configure Kubernetes manifests:

Deployment

Service

Ingress

ConfigMap

Secrets

Setup database:

Production PostgreSQL (managed service)

Automated backups

Read replicas (if needed)

Monitoring Setup
Setup Sentry:

Error tracking

Performance monitoring

Alerting

Setup Prometheus:

Metrics collection

Service discovery

Setup Grafana:

Dashboards for:

Request rate and latency

Error rate

Active bookings

Available seats

Redis cache hit rate

Database connection pool

Setup Alerting:

Slack/Telegram notifications on errors

Alert on 500 errors rate > 1%

Alert on p95 > 500ms

Documentation
Create OpenAPI 3.0 specification:

All endpoints

Request/response schemas

Error codes

Generate API client for Flutter:

Use openapi-generator

Generate Dart client

Write README.md:

Project overview

Tech stack

Local development setup

API documentation link

Write CONTRIBUTING.md:

Coding standards

Git workflow

Testing requirements

Pre-production Testing
Smoke test on staging environment:

All endpoints return 200

Database migrations applied

Seed data loaded

Security audit:

Check for exposed secrets

Check CORS configuration

Check rate limiting

Disaster recovery test:

Database restore from backup

Service restart

Release Checklist
All tests passing (CI/CD green)

Code coverage ≥ 80%

No critical bugs (P0/P1)

Performance tests passing (p95 < 200ms)

Monitoring and alerting configured

Documentation complete

Database migrations tested

Rollback plan documented

🔥 BLOCKERS / DEPENDENCIES
⚠️ SMS provider API key — Needed for auth service

⚠️ FCM server key — Needed for push notifications

⚠️ Payment provider credentials — Needed for deposit links

⚠️ Production domain names — Configure CORS and SSL

📝 NOTES
All database queries must use sqlc or ent for type safety

All repositories must use context for timeout handling

All errors must be logged with request ID for tracing

Fallback logging required for unknown statuses (F-024, F-044)

availableSeats < 0 → treat as 0 (F-015)

No public rating of marshals in API responses (FR-18)

Next Actions:

Start Iteration 0: Foundation

Set up Docker Compose

Create first migration

Initialize repository structure