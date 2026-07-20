# babycode — frontend

React + Vite + Monaco editor frontend for the babycode execution engine.

## Setup

```bash
npm install
npm run dev
```

Opens on `http://localhost:5173`. The dev server proxies `/api/*` requests to
`http://localhost:8080` (see `vite.config.js`), so make sure the Spring Boot
backend is running locally on port 8080 first.

## How it works

- **`src/api.js`** — talks to the backend's async submission flow: `submitCode()`
  POSTs and gets back a `QUEUED` submission immediately; `pollUntilComplete()`
  then polls `GET /api/submissions/{id}` every 800ms until `status` becomes
  `COMPLETED`, calling `onUpdate` on every poll so the UI can show live
  `QUEUED → RUNNING → COMPLETED` progress via the build strip.
- **`src/components/BuildStrip.jsx`** — the status indicator, styled to mirror
  a physical build/CI status light rather than a generic spinner. Reflects the
  real backend lifecycle, not a fake progress estimate.
- **`src/components/OutputPanel.jsx`** — renders stdout/stderr with
  verdict-specific styling (green for success, red for errors, amber for
  resource-limit verdicts).

## If you deploy the backend separately from this frontend

The Vite dev proxy only works in local dev. For a real deployment where the
frontend is served from a different origin than the backend, you'll need to
either:

1. Enable CORS on the Spring Boot backend (add a `WebMvcConfigurer` bean
   allowing your frontend's origin), or
2. Serve the built frontend (`npm run build` → `dist/`) as static files
   from Spring Boot itself (drop `dist/`'s contents into
   `src/main/resources/static/`), avoiding cross-origin requests entirely.

Option 2 is simpler for a single-deployment portfolio project.

## Build for production

```bash
npm run build
```

Outputs static files to `dist/`.
