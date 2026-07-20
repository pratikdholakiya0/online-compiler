const BASE_URL = import.meta.env.VITE_API_URL;

/**
 * Submits code and returns the initial Submission (status: QUEUED).
 */
export async function submitCode({ language, sourceCode, stdin }) {
  const res = await fetch(BASE_URL, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ language, sourceCode, stdIn: stdin ?? '' })
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.message || `Submit failed with status ${res.status}`);
  }

  return res.json();
}

/**
 * Fetches the current state of a submission by id.
 */
export async function getSubmission(id) {
  const res = await fetch(`${BASE_URL}/${id}`);
  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw new Error(body.message || `Fetch failed with status ${res.status}`);
  }
  return res.json();
}

/**
 * Polls a submission until it reaches COMPLETED, calling onUpdate after
 * every poll so the UI can reflect QUEUED -> RUNNING -> COMPLETED live.
 * Returns the final Submission. Stops automatically once completed or
 * once maxAttempts is hit (safety net against a runaway poll loop).
 */
export async function pollUntilComplete(id, { onUpdate, intervalMs = 800, maxAttempts = 30 } = {}) {
  for (let attempt = 0; attempt < maxAttempts; attempt++) {
    const submission = await getSubmission(id);
    onUpdate?.(submission);

    if (submission.status === 'COMPLETED') {
      return submission;
    }

    await new Promise((resolve) => setTimeout(resolve, intervalMs));
  }

  throw new Error('Timed out waiting for a result. The server may still be processing your submission.');
}
