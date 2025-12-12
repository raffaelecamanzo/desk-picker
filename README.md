# Desk Picker

Desk Picker is a Kubernetes-ready web app for reserving desks, viewing office occupancy, and managing layouts. This repo follows the specification in `.specify/specs/desk-picker.spec.md` and the delivery plan in `.specify/plans/desk-picker.plan.md`.

## Structure

- `backend/` — Quarkus (Java 21) REST API with health endpoints.
- `frontend/` — React + Vite SPA served by nginx.
- `infra/helm/` — Helm charts for backend and frontend with probes, ingress, and HPA.
- `infra/values/` — Environment-specific value files (dev/test/prod).
- `infra/argocd/` — Argo CD Applications for GitOps deployments.
- `ci/` — Jenkins pipeline draft.
- `schemas/` and `samples/` — Office JSON schema and sample data.

## Quick start (local)

### Backend
```bash
cd backend
mvn -B clean quarkus:dev
```

### Frontend
```bash
cd frontend
npm install
npm run dev -- --host
```

The frontend proxies `/api` to `http://localhost:8080` for local development (configure Vite proxy in `vite.config.ts`).

## Docker images

- Backend: `docker build -t desk-picker-backend:dev backend`
- Frontend: `docker build -t desk-picker-frontend:dev frontend`

## Kubernetes (Helm)

```bash
helm upgrade --install desk-picker-backend infra/helm/desk-picker-backend -f infra/values/values-dev.yaml
helm upgrade --install desk-picker-frontend infra/helm/desk-picker-frontend -f infra/values/values-dev.yaml
```

Ingress routes `/api` to the backend and `/` to the frontend.

## GitOps (Argo CD)

Argo CD applications under `infra/argocd` reference these Helm charts per environment. Update images and values via GitOps PRs; Argo CD syncs automatically.
