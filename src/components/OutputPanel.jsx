import './OutputPanel.css';

const VERDICT_COPY = {
  SUCCESS: 'Ran successfully',
  RUNTIME_ERROR: 'Runtime error',
  COMPILE_TIME_ERROR: 'Compile error',
  TIME_LIMIT_EXCEEDED: 'Exceeded the 5-second time limit',
  MEMORY_LIMIT_EXCEEDED: 'Exceeded the 256MB memory limit',
  INTERNAL_ERROR: 'Something went wrong on the server'
};

const VERDICT_TONE = {
  SUCCESS: 'green',
  RUNTIME_ERROR: 'red',
  COMPILE_TIME_ERROR: 'red',
  TIME_LIMIT_EXCEEDED: 'amber',
  MEMORY_LIMIT_EXCEEDED: 'amber',
  INTERNAL_ERROR: 'red'
};

export default function OutputPanel({ submission, error }) {
  if (error) {
    return (
      <div className="output-panel">
        <div className="output-panel__badge output-panel__badge--red">Request failed</div>
        <pre className="output-panel__block output-panel__block--red">{error}</pre>
      </div>
    );
  }

  if (!submission) {
    return (
      <div className="output-panel output-panel--empty">
        <p>Run your code to see output here.</p>
      </div>
    );
  }

  const result = submission.result;

  if (!result) {
    return (
      <div className="output-panel output-panel--empty">
        <p>Waiting for a result…</p>
      </div>
    );
  }

  const tone = VERDICT_TONE[result.status] ?? 'amber';
  const copy = VERDICT_COPY[result.status] ?? result.status;

  return (
    <div className="output-panel">
      <div className={`output-panel__badge output-panel__badge--${tone}`}>{copy}</div>

      {result.output ? (
        <>
          <div className="output-panel__label">stdout</div>
          <pre className="output-panel__block">{result.output}</pre>
        </>
      ) : null}

      {result.error ? (
        <>
          <div className="output-panel__label">stderr</div>
          <pre className="output-panel__block output-panel__block--red">{result.error}</pre>
        </>
      ) : null}

      {!result.output && !result.error ? (
        <p className="output-panel__muted">No output was produced.</p>
      ) : null}
    </div>
  );
}
