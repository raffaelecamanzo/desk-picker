# Desk Picker — Delivery Plan

Version: 1.0 • Date: 2025-12-10 • Owner: Engineering

## Goals

- Deliver a secure, scalable three-tier web app per spec.
- Tech stack: Java back-end, React.js SPA front-end, PostgreSQL DB, floor images on Amazon S3, all running on Kubernetes. Front-end served via Nginx pod. No cache layer initially.
- Support environments: dev, test, prod via config/values only.

## Architecture Overview

- Front-end: React.js responsive SPA built to static assets, served by `nginx` container in-cluster.
- Back-end: Java (Quarkus) stateless lightweight service, REST APIs (optionally gRPC later), OpenTelemetry, JWT session.
- Database: PostgreSQL (installation preference TBD; connection via env vars).
- Object Storage: Amazon S3 for floor images (per-environment buckets or prefixes).
- Kubernetes: Helm charts with env-specific values (`dev`, `test`, `prod`); Ingress with TLS; optional cert-manager; HorizontalPodAutoscaler.
- Email: Provider via SMTP or API (e.g., SendGrid). Configured via env vars.
- AuthN/Z: Google Sign-In (OIDC ID token verification); `hd` claim enforcement; admin via `DESK-PICKER-ADMIN` group.
- CI/CD: Jenkins for CI (build, test, scan, image push); Argo CD for CD (GitOps sync of Helm apps per environment).

## Environments & Configuration

- Namespaces: `desk-picker-dev`, `desk-picker-test`, `desk-picker-prod`.
- Images: Build once, promote by digest across environments.
- Secrets: Stored via SealedSecrets/External Secrets; never in git.
- Core env vars (representative):
  - Auth: `GOOGLE_CLIENT_ID`, `ALLOWED_GOOGLE_DOMAIN`, `JWT_SECRET`
  - Reservations: `RESERVATION_ALLOWED_NEXT_DAYS`
  - DB: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `DB_SSLMODE`
  - Email: `EMAIL_PROVIDER`, `EMAIL_FROM`, `SMTP_HOST`, `SMTP_PORT`, `SMTP_USER`, `SMTP_PASSWORD` (or `SENDGRID_API_KEY`)
  - S3: `S3_BUCKET`, `S3_REGION`, `S3_PREFIX`, `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY` (prefer workload identity/IRSA when available)
  - Observability: `OTEL_EXPORTER_OTLP_ENDPOINT`, `OTEL_RESOURCE_ATTRIBUTES`
  - App: `BASE_URL`, `APP_ENV`

## Kubernetes Deployment Model

- Helm chart(s):
  - `infra/helm/desk-picker-frontend` (Nginx Deployment + Service + Ingress; ConfigMap for `nginx.conf` and static assets mount)
  - `infra/helm/desk-picker-backend` (Quarkus Deployment + Service + Ingress)
  - Shared values: HPA, PDBs, resource requests/limits, probes (`/healthz`, `/readyz`).
- Ingress: Single hostname per env (e.g., `desk-picker.dev.example.com`), path routing `/` → frontend, `/api` → backend.
- Security: Namespace-scoped ServiceAccounts; NetworkPolicies; image scanning; run as non-root.
- CD: Argo CD Applications reference the Helm charts with per-environment values; optional App-of-Apps pattern to manage frontend and backend together per environment.

## Data & Migrations

- Schema managed via Flyway/Liquibase; forward-only migrations.
- Reservation persistence: one row per seat per day; unique `(seat_id, date)`.
- Indices on `user_id`, `date`, `seat_id` for performance.

## Work Breakdown Structure (WBS)

### 1. Project Bootstrap

- Repo structure, tooling, CI skeleton, issue templates.
- Quarkus starter (Java 21), React + Vite (TypeScript), ESLint/Prettier.
- Dockerfiles for backend and nginx-frontend; multi-stage builds.

### 2. Authentication & Roles

- Google Sign-In flow (frontend) and ID token verification (backend).
- Enforce `hd` claim; log full raw identity payload server-side.
- Determine admin via `DESK-PICKER-ADMIN` group; embed `is_admin` in JWT/session.

### 3. Domain Model & APIs (MVP)

- Entities: Office, Floor, Desk, Seat, Reservation, OfficeClosure, AvailabilityOverride.
- CRUD for user flows: upcoming reservations (me), create reservation, cancel reservation.
- Occupancy endpoints (today/tomorrow), user history counts.

### 4. Admin Features

- Upload office layout (images → S3, JSON → validate against `schemas/office.schema.json`).
- Closures management; availability overrides (office/floor/desk/seat, date/range).
- Trends and per-office occupancy views (aggregation queries).

### 5. Concurrency & Constraints

- Unique `(seat_id,date)` constraint; transactional booking; deterministic auto-assign.
- Reservation window enforcement via `RESERVATION_ALLOWED_NEXT_DAYS`.

### 6. Emails

- Reservation confirmation email; unavailability notification email.
- Templated HTML consistent with app styling.

### 7. Front-End Screens

- Login redirect; Dashboard (role-aware widgets); Reservation wizard (2 steps); My Reservations; Admin screens (Offices, Closures, Availability, Trends).
- Accessibility (WCAG 2.1 AA), responsive design, Core Web Vitals budgets.

### 8. Observability & Ops

- Structured logging, metrics, traces (OpenTelemetry).
- Health/readiness/startup probes; dashboards and basic alerts.

### 9. CI/CD & Kubernetes

- CI: Jenkins pipelines to build/test/scan, produce SBOM, and push container images (immutable tags + digests).
- CD: Argo CD Applications (one per service per env, or App-of-Apps) syncing Helm charts with `values-dev.yaml`, `values-test.yaml`, `values-prod.yaml`.
- GitOps: Jenkins updates GitOps manifests (image tag/digest or Helm values) via PR to the infra repo; Argo CD auto-syncs on merge.
- Secrets: SealedSecrets/External Secrets for sensitive config managed in the GitOps repo.

## Milestones & Acceptance

- M0 Bootstrap (Week 1): Repos, CI, base Dockerfiles, Hello World in cluster (dev).
- M1 Auth & Roles (Week 2): Google Sign-In, `hd` enforcement, admin flagging, identity logging.
- M2 Core Reservations (Week 3): Data model, booking/cancel, concurrency safeties, confirmation email.
- M3 Dashboard (Week 4): User widgets (upcoming, occupancy today/tomorrow, history).
- M4 Admin Ops (Week 5): Layout upload (S3 + schema validation), closures, availability overrides, unavailability notices.
- M5 Trends & Aggregations (Week 6): Per-office occupancy views and 6‑month trends.
- M6 Observability & Hardening (Week 7): Probes, HPA, PDBs, NetworkPolicies, dashboards, alerts.
- M7 CI/CD & Promotion (Week 8): Jenkins pipelines operational; Argo CD apps per env; GitOps promotion from test → prod.

## Risks & Mitigations

- Identity mapping unknown for office/groups: log full payload; feature flag mapping logic; admin UI override to set user office temporarily.
- S3 credentials in K8s: prefer IRSA/workload identity; fallback with short‑lived keys in External Secrets.
- No cache layer: monitor DB load; add Redis later behind an interface if needed.
- Front-end on Nginx: ensure correct SPA routing; test deep links; back-end `/api` path segregation.
- DB install TBD: abstract via env vars; support managed DB or in-cluster Helm chart for dev.
- Jenkins infra: ensure reliable agents with Docker/Podman; cache Maven/npm layers; secure credentials via Jenkins credentials store and least-privilege service accounts.
- Argo CD RBAC & drift: enforce project-level RBAC; restrict namespaces; monitor and alert on drift and sync failures.

## Deliverables

- Source repos with documented structure.
- Helm charts + `values-*.yaml`.
- Database migrations (Flyway/Liquibase).
- Email templates.
- OpenAPI spec for REST endpoints.
- Runbooks: Jenkins pipelines, Argo CD apps (sync/rollback), deployment, rollback, on-call basics.

## Definition of Done (per spec)

- Auth: Google Sign-In with `hd` check; admin via `DESK-PICKER-ADMIN`; identity payload logged.
- User Dashboard: upcoming + cancel, occupancy today/tomorrow, history counts.
- Admin Dashboard: per-office occupancy (today/tomorrow), closures/limited table, 6‑month trend.
- Configurability: add/modify offices via upload (images to S3, JSON validated) without rebuild.
- Booking: single/range, seat pick or auto-assign; unique `(seat_id,date)`; emails on booking; cancellations free the seat and send confirmation.
- K8s: frontend (Nginx) + backend (Quarkus) deployed with probes, HPA, and Ingress; env parity via values; observability in place; Argo CD Applications per environment perform deployments; Jenkins handles builds and GitOps updates.

## Initial Repo Structure (proposed)

- `backend/` — Quarkus app; Flyway/Liquibase migrations; OpenAPI.
- `frontend/` — React + Vite app; SPA.
- `infra/helm/desk-picker-backend/` — Helm chart.
- `infra/helm/desk-picker-frontend/` — Helm chart.
- `infra/values/values-dev.yaml` • `values-test.yaml` • `values-prod.yaml`.
- `infra/argocd/` — Argo CD Applications/AppProject manifests per environment.
- `ci/Jenkinsfile` (or root `Jenkinsfile`) — Jenkins pipeline definition(s).
- `schemas/office.schema.json` • `samples/office-sample.json`.

## Testing Strategy

- Unit tests (backend/frontend), contract tests for APIs, integration tests for booking flow and concurrency, e2e smoke per env.
- Performance smoke for occupancy/trends queries; accessibility checks on core views.
