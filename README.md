# Byte Compile — Online Compiler

A full-stack online code compiler/execution platform — React + Vite + Monaco editor frontend, backed by a Spring Boot execution engine.

**Live Demo:** https://compiler.pratikdholakiya.in

---

## Tech Stack

**Frontend**
- React
- Vite
- Monaco Editor (VS Code's editor, in-browser)

**Backend**
- Spring Boot (Java)
- Async job-queue style code execution (`QUEUED → RUNNING → COMPLETED`)

---

## Architecture

The frontend submits code to the backend, which queues it for execution and returns a submission ID immediately. The frontend then polls for the result and updates the UI live as the job progresses.

```
Browser (React/Vite)  --POST /api/submissions-->  Spring Boot Backend
       |                                                  |
       |<--- submission id (status: QUEUED) --------------|
       |
       |--GET /api/submissions/{id} (poll every 800ms)--->|
       |<--- status: QUEUED / RUNNING / COMPLETED --------|
```

---

## Project Structure

```
online-compiler/
├── src/
│   ├── api.js                     # Talks to backend async submission flow
│   ├── components/
│   │   ├── BuildStrip.jsx         # Live status indicator (build/CI-light style)
│   │   └── OutputPanel.jsx        # stdout/stderr with verdict-based styling
├── index.html
├── package.json
├── vite.config.js
└── README.md
```

*(Backend service assumed separate — update paths if it lives in this same repo.)*

---

## Getting Started

### Prerequisites
- Node.js + npm
- Java + Spring Boot backend running (see below)

### Frontend Setup

```
npm install
npm run dev
```

Runs on `http://localhost:5173`. The dev server proxies `/api/*` requests to `http://localhost:8080` (see `vite.config.js`) — so the Spring Boot backend must be running locally on port `8080` first.

### Backend Setup

Run the Spring Boot backend on port `8080` (default). It exposes:
- `POST /api/submissions` — submit code, returns a `QUEUED` submission
- `GET /api/submissions/{id}` — poll submission status/result

---

## How It Works

- **`src/api.js`** — `submitCode()` POSTs code and immediately gets back a `QUEUED` submission. `pollUntilComplete()` polls `GET /api/submissions/{id}` every 800ms until `status` becomes `COMPLETED`, invoking `onUpdate` on every poll so the UI reflects live `QUEUED → RUNNING → COMPLETED` progress.
- **`BuildStrip.jsx`** — status indicator styled like a physical CI/build light rather than a generic spinner; reflects the real backend lifecycle instead of a fake progress bar.
- **`OutputPanel.jsx`** — renders stdout/stderr with verdict-specific styling: green (success), red (errors), amber (resource-limit verdicts).

---

## Deployment

If frontend and backend are deployed separately (different origins), choose one:

1. **Enable CORS** on the Spring Boot backend (add a `WebMvcConfigurer` bean allowing your frontend's origin), **or**
2. **Serve the frontend as static files from Spring Boot** — run `npm run build` (outputs to `dist/`) and drop the contents into `src/main/resources/static/`. This avoids cross-origin requests entirely and is the simpler option for a single-deployment portfolio project.

### Build for Production

```
npm run build
```

Outputs static files to `dist/`.

---

## License

Add a license of your choice (MIT recommended for portfolio/open-source projects).
