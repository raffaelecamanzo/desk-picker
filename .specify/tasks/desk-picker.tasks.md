# Desk Picker — Task Breakdown

Version: 1.0 • Date: 2025-12-10

## Conventions

- All changes via PR with review; include spec/plan reference in PR description.
- Each task includes DoD and dependencies. Add tests and docs where applicable.

## M0 — Bootstrap

- [x] M0-T1 Repo scaffolding
  - [x] M0-T1.1 Create `backend/` (Quarkus, Java 21) and `frontend/` (React+Vite TS)
  - [x] M0-T1.2 Create `infra/helm/*`, `infra/values/*`, `infra/argocd/*`
  - [x] M0-T1.3 Add `ci/Jenkinsfile`, `schemas/`, `samples/`, top-level README
  - DoD: Repos and folders committed, README with quick start
  - Dependencies: —
  - Parallelizable with: —

- [x] M0-T2 Dockerfiles
  - [x] M0-T2.1 Backend multi-stage Quarkus container
  - [x] M0-T2.2 Nginx-based static site image for SPA
  - DoD: Local `docker build` succeeds for both images
  - Dependencies: M0-T1
  - Parallelizable with: M0-T3

- [x] M0-T3 Hello World deployments (dev)
  - [x] M0-T3.1 Minimal Helm charts for frontend/backend with probes
  - [x] M0-T3.2 Ingress routing `/` → frontend and `/api` → backend
  - DoD: App accessible in dev namespace via hostname; health endpoints green
  - Dependencies: M0-T1, M0-T2
  - Parallelizable with: —

## M1 — Auth & Roles

- [ ] M1-T1 Google Sign-In (frontend)
  - [ ] M1-T1.1 Implement login button/flow
  - [ ] M1-T1.2 Retrieve ID token; forward to backend
  - DoD: Auth UI appears; token sent to backend
  - Dependencies: M0-T3
  - Parallelizable with: M1-T2, M1-T3

- [ ] M1-T2 ID token verification (backend)
  - [ ] M1-T2.1 Verify signature, `aud`, `iss`, `exp`, optional `nonce`, email verified
  - [ ] M1-T2.2 Enforce `hd` via `ALLOWED_GOOGLE_DOMAIN`
  - [ ] M1-T2.3 Log full raw identity payload (structured)
  - DoD: Invalid/mismatched `hd` denied; logs include full payload
  - Dependencies: M0-T3
  - Parallelizable with: M1-T1, M1-T3

- [ ] M1-T3 Role detection (`DESK-PICKER-ADMIN`)
  - [ ] M1-T3.1 Determine admin via groups in identity or directory lookup
  - [ ] M1-T3.2 Persist `is_admin` in JWT/session; expose via `/api/me`
  - DoD: Admin flag reflected in `/api/me` and UI adapts
  - Dependencies: M1-T2
  - Parallelizable with: M1-T1

## M2 — Domain, Data & Core Reservations

- [ ] M2-T1 Data model & migrations
  - [ ] M2-T1.1 Create Flyway/Liquibase migrations (Office, Floor, Desk, Seat, Reservation, OfficeClosure, AvailabilityOverride)
  - [ ] M2-T1.2 Add unique constraint `(seat_id, date)`; add indices on `user_id`, `date`, `seat_id`
  - DoD: Migrations apply cleanly; constraints enforced
  - Dependencies: M0-T1
  - Parallelizable with: M1 tasks

- [ ] M2-T2 Booking API
  - [ ] M2-T2.1 Create reservation for single day or expanded date range
  - [ ] M2-T2.2 Deterministic auto-assign strategy
  - [ ] M2-T2.3 Enforce `RESERVATION_ALLOWED_NEXT_DAYS`
  - [ ] M2-T2.4 Transactional create with retries; respect unique constraint
  - DoD: Parallel booking tests prevent double-booking; clear feedback for out-of-window
  - Dependencies: M2-T1, M1-T2
  - Parallelizable with: M2-T3

- [ ] M2-T3 Cancel API
  - [ ] M2-T3.1 Cancel one-day reservation; free seat
  - [ ] M2-T3.2 Ensure idempotency on repeated cancel
  - DoD: Seat availability reflects cancellation; idempotent behavior
  - Dependencies: M2-T1
  - Parallelizable with: M2-T2

## M3 — Dashboard (User)

- [ ] M3-T1 Upcoming reservations (endpoint + UI)
  - [ ] M3-T1.1 Backend endpoint (self upcoming list)
  - [ ] M3-T1.2 Frontend table with per-row Cancel button
  - DoD: Matches spec; cancel updates UI
  - Dependencies: M2-T2, M2-T3
  - Parallelizable with: M3-T2, M3-T3

- [ ] M3-T2 Occupancy widgets (today/tomorrow)
  - [ ] M3-T2.1 Backend computations for user's office
  - [ ] M3-T2.2 Frontend widgets render
  - DoD: Widgets render correct values; handles no-data
  - Dependencies: M2-T1
  - Parallelizable with: M3-T1, M3-T3

- [ ] M3-T3 Reservation history widget
  - [ ] M3-T3.1 Backend counts (1, 3, 6 months)
  - [ ] M3-T3.2 Frontend widget render
  - DoD: Values match DB queries; unit-tested
  - Dependencies: M2-T1
  - Parallelizable with: M3-T1, M3-T2

## M4 — Admin Operations

- [ ] M4-T1 Per-office occupancy (today/tomorrow)
  - [ ] M4-T1.1 Backend aggregation across all offices
  - [ ] M4-T1.2 Admin UI table render
  - DoD: List shows all offices with correct figures
  - Dependencies: M2-T1
  - Parallelizable with: M4-T2, M4-T3, M4-T4

- [ ] M4-T2 Closures & limited availability
  - [ ] M4-T2.1 Endpoints for closures and overrides (office/floor/desk/seat, date/range)
  - [ ] M4-T2.2 Admin UI to manage entries
  - DoD: Overrides affect availability queries; inputs validated
  - Dependencies: M2-T1
  - Parallelizable with: M4-T1, M4-T3, M4-T4

- [ ] M4-T3 Office layout upload
  - [ ] M4-T3.1 Upload floor images to S3
  - [ ] M4-T3.2 Upload JSON description; validate against `schemas/office.schema.json`
  - DoD: Valid JSON accepted/stored; invalid rejected; images retrievable per env policy
  - Dependencies: M0-T3
  - Parallelizable with: M4-T1, M4-T2, M4-T4

- [ ] M4-T4 Trends (6 months)
  - [ ] M4-T4.1 Backend aggregates per office
  - [ ] M4-T4.2 Admin graph render
  - DoD: Chart renders with accurate aggregates
  - Dependencies: M2-T1
  - Parallelizable with: M4-T1, M4-T2, M4-T3

## M5 — Emails

- [ ] M5-T1 Reservation confirmation
  - [ ] M5-T1.1 HTML template aligned with app
  - [ ] M5-T1.2 Send on successful reservation
  - DoD: Email delivered with office/floor/desk/seat and dates
  - Dependencies: M2-T2
  - Parallelizable with: M5-T2, M5-T3

- [ ] M5-T2 Unavailability notifications
  - [ ] M5-T2.1 Detect impacted reservations on new unavailability
  - [ ] M5-T2.2 Send notifications to users
  - DoD: Impacted users receive email with reason and dates; retries/logging on failures
  - Dependencies: M4-T2
  - Parallelizable with: M5-T1, M5-T3

- [ ] M5-T3 Cancellation confirmation
  - [ ] M5-T3.1 Send brief confirmation email on cancel
  - DoD: Email delivered on cancellation with reservation identifiers and date; logged
  - Dependencies: M2-T3
  - Parallelizable with: M5-T1, M5-T2

## M6 — Observability & Ops

- [ ] M6-T1 Probes & health endpoints
  - [ ] M6-T1.1 Backend `/healthz`, `/readyz`, startup probe
  - [ ] M6-T1.2 Nginx health check for frontend
  - DoD: K8s shows healthy; startup delays respected
  - Dependencies: M0-T3
  - Parallelizable with: M6-T2, M6-T3

- [ ] M6-T2 OpenTelemetry
  - [ ] M6-T2.1 Structured logs for key flows (auth, booking, cancel, occupancy, uploads)
  - [ ] M6-T2.2 Metrics and traces exported
  - DoD: Data visible in collector/exporter; dashboards exist
  - Dependencies: M2-T2, M2-T3
  - Parallelizable with: M6-T1, M6-T3

- [ ] M6-T3 Security hardening
  - [ ] M6-T3.1 RBAC, NetworkPolicies, run as non-root
  - [ ] M6-T3.2 Image scan stage; secrets via Sealed/External Secrets
  - DoD: Policies applied; scans pass; no secrets in images
  - Dependencies: M0-T3
  - Parallelizable with: M6-T1, M6-T2

## M7 — CI/CD (Jenkins + Argo CD)

- [ ] M7-T1 Jenkins pipelines
  - [ ] M7-T1.1 Build/test/scan; SBOM; push images (immutable tags/digests)
  - [ ] M7-T1.2 Cache Maven/npm layers
  - DoD: Pipeline runs on PR and main; artifacts and images published
  - Dependencies: M0-T2, M0-T1
  - Parallelizable with: M7-T2, M7-T3

- [ ] M7-T2 GitOps updates
  - [ ] M7-T2.1 Jenkins updates image tags/digests or Helm values via PR to `infra/argocd`
  - DoD: PR auto-created/merged with checks; audit trail exists
  - Dependencies: M7-T1
  - Parallelizable with: —

- [ ] M7-T3 Argo CD Applications
  - [ ] M7-T3.1 Create AppProject per env and Applications (frontend/backend or App-of-Apps)
  - [ ] M7-T3.2 Configure sync policies and rollback
  - DoD: Auto-sync on merge; rollbacks tested via Argo CD UI/CLI
  - Dependencies: M0-T3
  - Parallelizable with: M7-T1

## Front-End (Shared Across Milestones)

- [ ] FE-T1 SPA routing via Nginx
  - [ ] FE-T1.1 Configure `nginx.conf` for history API fallback
  - [ ] FE-T1.2 Cache headers for static assets; content hashes
  - DoD: Deep links work; assets cached with content hashes
  - Dependencies: M0-T2, M0-T3
  - Parallelizable with: FE-T2

- [ ] FE-T2 Accessibility & performance
  - [ ] FE-T2.1 WCAG 2.1 AA checks automated
  - [ ] FE-T2.2 Core Web Vitals budgets enforced in CI (LCP ≤ 2.5s, CLS < 0.1, INP/TBT < 200ms)
  - DoD: Automated checks pass; Web Vitals meet numeric budgets in CI
  - Dependencies: M3 tasks
  - Parallelizable with: FE-T1

## Infrastructure & Configuration

- [ ] INF-T1 Helm values per env
  - [ ] INF-T1.1 Create `values-dev.yaml`, `values-test.yaml`, `values-prod.yaml`
  - [ ] INF-T1.2 Hostnames, replicas, resources, HPA, secrets refs
  - DoD: Only deltas per env; same images across envs
  - Dependencies: M0-T3
  - Parallelizable with: INF-T2, INF-T3, INF-T4

- [ ] INF-T2 Secrets management
  - [ ] INF-T2.1 Configure External Secrets or SealedSecrets (DB, email, S3, JWT)
  - [ ] INF-T2.2 Namespace-scoped ServiceAccounts
  - DoD: No plaintext secrets in git; pods receive secrets successfully
  - Dependencies: M0-T3
  - Parallelizable with: INF-T1, INF-T3, INF-T4

- [ ] INF-T3 S3 bucket setup
  - [ ] INF-T3.1 Create buckets/prefixes per env; IAM (IRSA/workload identity preferred)
  - [ ] INF-T3.2 Lifecycle policies
  - DoD: Uploads succeed from cluster; least-privilege access
  - Dependencies: — (can start after M0-T3 cluster bootstrap)
  - Parallelizable with: INF-T1, INF-T2, INF-T4

- [ ] INF-T4 Database connectivity
  - [ ] INF-T4.1 Env-var DB parameters; SSL/TLS
  - [ ] INF-T4.2 Connection pooling if needed
  - DoD: App connects in all envs; timeouts and retries tuned
  - Dependencies: M2-T1
  - Parallelizable with: INF-T1, INF-T2, INF-T3

## Testing & Quality Gates

- [ ] TQ-T1 Backend tests & coverage
  - [ ] TQ-T1.1 Unit/integration tests; contract tests for APIs
  - [ ] TQ-T1.2 Concurrency tests for bookings
  - DoD: Coverage per policy; green tests in CI
  - Dependencies: M2 tasks
  - Parallelizable with: TQ-T2, TQ-T3

- [ ] TQ-T2 Frontend tests
  - [ ] TQ-T2.1 Unit and component tests
  - [ ] TQ-T2.2 e2e smoke for booking/cancel
  - DoD: CI runs tests and lints; artifacts produced
  - Dependencies: M3 tasks
  - Parallelizable with: TQ-T1, TQ-T3

- [ ] TQ-T3 Security & compliance
  - [ ] TQ-T3.1 SAST/DAST, dependency scans, license checks
  - [ ] TQ-T3.2 SBOM generation in CI
  - DoD: Gates pass before merge/release
  - Dependencies: M7-T1
  - Parallelizable with: TQ-T1, TQ-T2

## Acceptance Matrix (Traceability)

- Each task maps to spec sections 2–19 and plan milestones M0–M7.
- DoD per task must be met in dev; promotion to test/prod via Argo CD after approvals.
