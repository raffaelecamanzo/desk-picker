# Desk Picker — Product & Technical Specification

Version: 1.0 • Date: 2025-12-09

## 1. Context & Purpose

Desk Picker is a web application that allows company employees/collaborators to reserve a desk in company offices. All users are authenticated and authorized. The application supports a user and admin role. The system must be configurable to add/modify offices, layouts, and seat configurations without rebuilds.

## 2. Authentication & Authorization

### 2.1 Google Sign-In (OIDC)

- Integrate with Google Workspace / Google Sign-In ("Login with Google").
- Server verifies the Google ID token signature and claims using Google's public keys and validates the token audience, issuer, expiry, nonce (if used), and email verification.
- Enforce hosted domain: server checks `hd` claim equals the organization domain provided via `ALLOWED_GOOGLE_DOMAIN` environment variable. If `hd` is not the specified org, deny access.
- On successful login, the server logs the full raw identity payload received from Google (server-side, structured logs) to aid later mapping of office and groups.

### 2.2 User Profile & Office Membership

- Each user belongs to a specific office. This information comes from the identity provider.
- Because the exact structure is unknown, on login the server logs the full identity payload. Office mapping logic will be added/refined once data fields are confirmed.

### 2.3 Roles and Group Membership

- Roles: `User` (standard) and `Admin`.
- An Admin is any authenticated user who is a member of the `DESK-PICKER-ADMIN` group.
- At login, the server determines group memberships (from identity claims and/or a directory lookup) and persists derived role info (e.g., `role=admin` flag) in the user profile/session/JWT. The front-end adapts the UI accordingly.

### 2.4 Sessions & Tokens

- Back-end remains stateless. Use token-based sessions (e.g., signed JWT stored in an HTTP-only secure cookie or Bearer token). Include minimal identity and role claims. Token TTL and refresh strategy follow security best practices.

## 3. Post-Login Landing: Dashboard

- After login, users land on the Dashboard with widgets summarizing reservation data and a primary action to start the reservation wizard.
- Role and office membership determine visible widgets and data scope.

## 4. Dashboard — User View

1. Upcoming reservations table — Lists upcoming reservations for the current user. Each row shows office, date, floor/desk/seat identifiers, and includes a Cancel button for that day.

1. Current day office occupancy (user office) — Shows the percentage of reserved spots for the user's office for today.

1. Next day office occupancy (user office) — Shows the percentage of reserved spots for the user's office for tomorrow.

1. User reservation history — Shows counts of the user's reservations for the last month, last 3 months, and last 6 months.

## 5. Dashboard — Admin View

Admins see all widgets available to standard users plus:

1. Upcoming reservations (admin's own upcoming list) — Same table and cancel capability as the user view for the admin's personal reservations.

1. Per-office occupancy (current day) — For each configured office, shows today's reserved-spot percentage.

1. Per-office occupancy (next day) — For each configured office, shows tomorrow's reserved-spot percentage.

1. Office closures & limited availability table — For each office, lists closures and periods with limited seat availability.

1. Office reservations trend — For each office, a graph of the average number of reservations over the last six months.

## 6. Offices & Layouts

### 6.1 Configurability

- There are currently 6 offices. The app must permit adding/removing offices, modifying layouts, and adjusting seat configurations without rebuilding.
- Admin UI enables configuration changes; the system reads updated configuration dynamically.

### 6.2 Hierarchy

- Office → Floors (1..n) → Desks (1..n per floor) → Seats (1..n per desk).

### 6.3 Representation

- Images: one image per floor, used in the UI to visualize placement.
- JSON: a JSON file describing the office, floors, desks, and seats.

## 7. Office Description JSON

- JSON Schema location: `schemas/office.schema.json`
- Sample JSON file: `samples/office-sample.json`
- The app validates uploaded JSON against the schema and uses it to render layouts and compute availability.

## 8. User Operations

### 8.1 Book a Spot (Wizard)

Entry point: Dashboard button opens a two-step reservation wizard.

Step 1 – Office and date selection

- Select office (default: user's office).
- Select a single date or a date range.

Step 2 – Seat selection

- Show desks and available seats for the chosen date(s).
- User can either select a specific seat (desk+seat) or request automatic assignment of the first available seat for the date or date range.

Reservation behavior

- On selection (specific or automatic), the system reserves the seat for all selected date(s), making it unavailable to others for those day(s), and ties it to the current user.

Feedback & notifications

- After creation, the UI displays a success confirmation.
- The system sends an email to the user with reservation details (office, date[s], seat, etc.).

Concurrency requirement

- Prevent double-bookings for the same seat on the same date(s) under concurrent attempts via strong safeguards:
  - Persist reservations as one record per seat-day (expand ranges into discrete dates) with a unique constraint on `(seat_id, date)`.
  - Use transactions and retry logic; optionally use optimistic concurrency checks.

### 8.2 Cancel a Reservation

- From the upcoming reservations table, a Cancel button triggers a confirmation dialog.
- On confirm: cancel the reservation for that specific day, free up the seat for that date, and update views accordingly.
- Email on cancellation, send a brief cancellation confirmation.

## 9. Admin Characteristics

- Admins are authenticated employees with the `DESK-PICKER-ADMIN` group membership; they see all user features plus admin capabilities. Role detection as in Section 2.

## 10. Admin-Specific Operations

### 10.1 Add a New Office

- Admin can upload one or more floor images and one JSON office description.
- The JSON must validate against `schemas/office.schema.json`.

### 10.2 Insert / Update Office Closing Days

- Admin selects dates to mark the office closed. Closed dates are unavailable for reservations.

### 10.3 Floor / Desk / Seat Availability Management

- Admin can mark unavailable, for a date or date range: an entire office, a floor, a desk, or a seat.

### 10.4 Existing Reservations on Newly Unavailable Resources

- When marking resources unavailable for a date/range, the system detects impacted reservations and sends email notifications to impacted users informing that their reservation is no longer valid and explaining the reason (new unavailability at office/floor/desk/seat level).

## 11. User Experience

- Use sample images in `samples/` as inspiration for the UI, not as strict designs.
  - `samples/user-dashboard.png`
  - `samples/admin-dashboard.png`
  - `samples/book-a-spot-1-2.png`
  - `samples/book-a-spot-2-2.png`
- Use similar look/feel for the HTML body of notification emails.

## 12. Non-Functional Requirements

- Stateless back-end: No in-memory session state required to serve requests; token-based auth; horizontal scaling supported; no sticky sessions.
- Reliability & concurrency: Unique constraint on `(seat_id, date)`; transactional reservation creation; idempotent APIs where relevant; retries with backoff on safe operations.
- Observability: Structured logs; log full raw identity data on login; metrics and traces for reservation flows; health/readiness endpoints.
- Security: HTTPS everywhere; strict token validation; enforce `hd` domain; least-privilege access; input validation; secrets not stored in code.
- Configurability: Offices/layouts/seats can be updated at runtime via admin UI; assets stored in managed storage; JSON validated against schema.
- Accessibility & performance: Front-end is responsive, keyboard accessible, and performant per modern best practices.

## 13. Data Model (Conceptual)

- User: `id`, `email`, `name`, `office_id` (nullable until mapping defined), `is_admin`.
- Office: `id`, `name`, `code`.
- Floor: `id`, `office_id`, `name`, `level`, `image_url`.
- Desk: `id`, `floor_id`, `code`.
- Seat: `id`, `desk_id`, `code`.
- Reservation: `id`, `user_id`, `seat_id`, `date` (one row per day), `created_at`.
  - Constraint: unique `(seat_id, date)`.
- OfficeClosure: `id`, `office_id`, `start_date`, `end_date` (inclusive).
- AvailabilityOverride: `id`, `scope_type` (`office|floor|desk|seat`), `scope_id`, `start_date`, `end_date`, `status` (`unavailable|limited`), optional capacity for limited.

## 14. API Surface (Illustrative)

- Auth
  - `GET /api/auth/login/google` (client-initiated flow)
  - `POST /api/auth/callback/google` (server verifies ID token, sets session/JWT)
  - `GET /api/me` (returns user profile incl. `is_admin`, office)
- User Reservations
  - `GET /api/reservations/upcoming` (current user)
  - `POST /api/reservations` (create; payload: office, dates or date range, seat selection or `autoAssign=true`)
  - `DELETE /api/reservations/{reservationId}` (cancel one day)
- Occupancy & History
  - `GET /api/occupancy/office/{officeId}?date=YYYY-MM-DD`
  - `GET /api/history/user` (counts: 1, 3, 6 months)
- Admin
  - `GET/POST /api/admin/offices` (list/create)
  - `POST /api/admin/offices/{officeId}/layout` (upload images + JSON; validate schema)
  - `POST /api/admin/closures` (create office closure window)
  - `POST /api/admin/availability` (create availability overrides)
  - `GET /api/admin/occupancy?date=today|tomorrow` (all offices)
  - `GET /api/admin/trends?months=6` (per-office averages)

## 15. Reservation Logic & Edge Cases

- Date ranges expand into per-day reservations; skip closed/unavailable days and return clear feedback for skipped days.
- Automatic assignment selects the first available seat per day deterministically (stable ordering by office/floor/desk/seat code) to produce predictable results.
- Time zones: All date computations use the office-local time zone; UI clarifies the applied time zone.
- Cancellations: Allowed for future dates; configurable cutoff window (e.g., until 23:59 prior day) — if implemented, enforce consistently.
- Reservations are allowed for the next number of days provided via `RESERVATION_ALLOWED_NEXT_DAYS` environment variable (plus today), if the user attempts a reservation out of the allowed window the attempt must fail with a clear feedback.

## 16. Emails

- Templates: HTML emails styled consistent with the app.
- Reservation confirmation: includes office, floor/desk/seat, date(s), and a cancellation link or instructions.
- Unavailability notifications (admin-triggered): clearly state invalidation reason and affected dates; provide guidance for rebooking.

## 17. Front-End Views

- Login (redirects to Google Sign-In).
- Dashboard (role-aware widgets).
- Reservation Wizard (Step 1: office/date; Step 2: seat/auto assignment; final confirmation).
- My Reservations (table with cancel actions — can be part of Dashboard).
- Admin: Offices (add/update), Closures, Availability Management, Trends & Occupancy views.

## 18. Configuration & Environment

- Environment variables (examples): `GOOGLE_CLIENT_ID`, `ALLOWED_GOOGLE_DOMAIN`, `RESERVATION_ALLOWED_NEXT_DAYS`, `SESSION_SECRET`/`JWT_SECRET`, email provider settings, base URL, file storage bucket.
- Schema and sample locations are fixed: `schemas/office.schema.json`, `samples/office-sample.json`.
- No rebuilds required for new offices/layout changes; processed via admin uploads and persisted configuration.

## 19. Acceptance Criteria (Traceability)

- Google Sign-In enforced; server verifies token and `hd` claim; non-matching `hd` denied.
- Raw identity payload logged on login.
- Admin role derived from `DESK-PICKER-ADMIN` group; UI adapts.
- User Dashboard shows upcoming reservations (with cancel), today's and tomorrow's office occupancy, and history counts.
- Admin Dashboard adds per-office occupancy (today/tomorrow), closures/limited table, and 6-month trend.
- Offices configurable; floor images + JSON validated against schema; changes effective without rebuild.
- Reservation wizard supports single day and date range; seat pick or auto-assign.
- Concurrency-safe: no double bookings; unique `(seat_id, date)` enforced.
- Confirmation email sent on successful reservation.
- Cancellation confirmation email sent on successful cancellation.
- Cancel flow works with confirmation dialog; availability updates accordingly.
- Admin unavailability invalidates impacted reservations and sends notification emails.

---

This specification reorganizes and clarifies the provided requirements without altering functional constraints. It makes implicit constraints explicit (stateless back-end, concurrency model, data constraints, and API surface) while preserving the original intent.
