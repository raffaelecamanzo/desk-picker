# Desk Picker Constitution

## Core Principles

### I. Kubernetes-Native, Environment-Parity

Run the same container images across `dev`, `test`, and `prod`, with all differences expressed as configuration (`env`, ConfigMaps, Secrets, values files). Images are immutable and reproducible; deployments are declarative and idempotent. Manifests are shared with environment overlays to ensure parity and reduce drift.

### II. Stateless Back-End & Horizontal Scalability (NON-NEGOTIABLE)

All back-end services are 12‑factor and stateless: no local disk/session affinity, no in-memory state required to serve requests, and no sticky sessions. State is externalized (database, cache, object storage). Endpoints are idempotent where applicable to support retries. Services define HPA targets and scale horizontally under load.

### III. Modern, Lightweight, Accessible Front-End

The front-end prioritizes fast start-up, small bundles, and usability. Core Web Vitals must meet targets (LCP ≤ 2.5s, CLS < 0.1, INP/TBT < 200ms on mid‑tier hardware). Accessibility meets WCAG 2.1 AA. Design remains simple, responsive, and touch-friendly, with progressive enhancement and graceful degradation.

### IV. Observability & Operability First

Services emit structured logs, metrics, and traces with OpenTelemetry. Health, readiness, and startup probes are mandatory. SLOs and error budgets guide release and incident decisions. Dashboards and alerts are provisioned with code and maintained with the service.

### V. Security, Versioning & Simplicity

Security by default (least privilege RBAC, network policies, image signing, SBOM, SAST/DAST, regular dependency updates). APIs and artifacts follow Semantic Versioning and maintain backward compatibility within a major version. Prefer simplicity and YAGNI; justify complexity with measurable value.

## Architecture & Technology Standards

- Topology: Three-tier system with a modern SPA/SSR front-end, stateless back-end APIs, and a relational database. Optional shared services: cache (e.g., Redis), object storage for assets, and a message broker when needed.
- Kubernetes:
  - Packaging: Helm charts with environment-specific values files or kustomize overlays (`dev`, `test`, `prod`).
  - Separation: Distinct clusters or namespaces per environment; promotion reuses the same image digest.
  - Resilience: `readinessProbe`, `livenessProbe`, `startupProbe`, PodDisruptionBudgets, and anti‑affinity where appropriate.
  - Scaling: `resources.requests/limits` required; HPA enabled with CPU and/or custom metrics.
  - Networking: Ingress with TLS (cert‑manager), strict NetworkPolicies, and service mesh optional.
  - Configuration: ConfigMaps for non-secrets; Secrets via External Secrets or SealedSecrets. No credentials in images.
  - Security: Namespace‑scoped ServiceAccounts with minimum RBAC; signed, vulnerability‑scanned images.
- Front-End:
  - Goals: Small bundles (≤ 170KB gzipped JS, ≤ 50KB gzipped CSS for critical path), fast TTI, offline‑safe routing errors.
  - UX: WCAG 2.1 AA, responsive layouts, keyboard and screen‑reader support, consistent design tokens.
  - Delivery: Static assets served via CDN where available, long‑cache with content hashes, HTTP/2 or HTTP/3.
  - APIs: Communicate over HTTPS, robust error states, retry/backoff on idempotent calls, feature flags for UI rollout.
- Back-End:
  - Interfaces: REST and/or gRPC with explicit versioning; pagination and filtering for list endpoints.
  - Statelessness: No local disk dependence; ephemeral storage only. Sessions are token‑based (OIDC/JWT); server‑side sessions require external stores.
  - Reliability: Idempotency keys for mutating endpoints where needed; timeouts, circuit breakers, and retries configured.
  - Config: Exclusively via environment variables/config files; 12‑factor compliant. Provide `/healthz` and `/readyz`.
- Database:
  - Choice: Managed relational database (e.g., PostgreSQL). TLS in transit, encryption at rest. Connection pooling (e.g., PgBouncer) recommended.
  - Migrations: Versioned, forward‑only migrations applied via automated jobs; backwards‑compatible rollout patterns.
  - Reliability: Regular backups and tested restore procedures; clearly defined RPO/RTO; separate instances per environment.
- CI/CD:
  - Build once, promote many: Same image promoted from `dev` → `test` → `prod`.
  - Gates: Static analysis, unit/contract/integration tests, vulnerability and license scans, and deploy previews for UI.
  - Deployment: Rolling updates by default; blue/green or canary for high‑risk changes; automatic rollback on health failures.
- Configuration by Environment:
  - Values files: `values-dev.yaml`, `values-test.yaml`, `values-prod.yaml` (or overlays) capture only deltas.
  - Secrets: Provisioned per environment via a secret manager; never committed to VCS.
  - Flags: Feature flags toggle behavior without redeploys; safe defaults in `prod`.

## Development Workflow & Quality Gates

- Branching & Reviews: Trunk‑based with short‑lived feature branches. All changes via PR with at least one review (`prod`‑impacting changes require two) and CODEOWNERS where applicable.
- Testing Strategy: Test‑first where practical. Minimum 80% line coverage for critical services; contract tests for API boundaries; integration tests for cross‑service flows; e2e smoke tests per environment.
- Quality & Security: Mandatory linting, formatting, type checking; SAST/DAST, SBOM generation, dependency/update checks, and container image scans on every build.
- Release Management: Semantic versioning for services and Helm charts. Release notes include user‑visible changes, migration steps, and rollback plan.
- Deployment Promotion: Automated pipelines with manual approval gates from `test` → `prod`. Database migrations run as pre‑deploy jobs and are safe to apply ahead of code when required.
- Operability: Each service ships dashboards, SLOs, alerts, and runbooks. Incidents have postmortems with action items tracked to closure.
- Documentation: Architecture decision records (ADRs) for significant choices; README and operational docs co‑located with code.

## Governance

- Authority: This constitution supersedes other practice guides for the three‑tier Kubernetes application.
- Compliance: PR templates include a constitution checklist; reviewers verify statelessness, observability, security, and deployment parity.
- Amendments: Changes require an RFC, designated owners, risk assessment, and a migration/deprecation plan with timelines.
- Exceptions: Granted sparingly, time‑boxed, with named owner and explicit exit criteria; documented alongside the service.
- Audits: Quarterly reviews of SLOs, security posture, dependency freshness, and Kubernetes resource correctness.

**Version**: 1.0.0 | **Ratified**: 2025-12-08 | **Last Amended**: 2025-12-08
