import { useState } from 'react';
import Editor from '@monaco-editor/react';
import { LANGUAGES } from './languages';
import { submitCode, pollUntilComplete } from './api';
import BuildStrip from './components/BuildStrip.jsx';
import OutputPanel from './components/OutputPanel.jsx';
import './App.css';

export default function App() {
  const [language, setLanguage] = useState(LANGUAGES[0]);
  const [sourceCode, setSourceCode] = useState(LANGUAGES[0].starter);
  const [stdin, setStdin] = useState('');
  const [submission, setSubmission] = useState(null);
  const [requestError, setRequestError] = useState(null);
  const [isRunning, setIsRunning] = useState(false);

  function handleLanguageChange(value) {
    const next = LANGUAGES.find((l) => l.value === value);
    setLanguage(next);
    setSourceCode(next.starter);
  }

  async function handleRun() {
    setIsRunning(true);
    setRequestError(null);
    setSubmission(null);

    try {
      const initial = await submitCode({
        language: language.value,
        sourceCode,
        stdin
      });
      setSubmission(initial);

      const final = await pollUntilComplete(initial.id, {
        onUpdate: setSubmission
      });
      setSubmission(final);
    } catch (err) {
      setRequestError(err.message);
    } finally {
      setIsRunning(false);
    }
  }

  return (
    <div className="app">
      <header className="app__header">
        <div className="app__brand">
          <span className="app__brand-dot" />
          ByteCode
        </div>

        <select
          className="app__language-select"
          value={language.value}
          onChange={(e) => handleLanguageChange(e.target.value)}
          disabled={isRunning}
        >
          {LANGUAGES.map((l) => (
            <option key={l.value} value={l.value}>
              {l.label}
            </option>
          ))}
        </select>

        <button className="app__run-button" onClick={handleRun} disabled={isRunning}>
          {isRunning ? 'Running…' : 'Run'}
        </button>

        <div className="app__strip">
          <BuildStrip status={submission?.status} verdict={submission?.result?.status} />
        </div>
      </header>

      <main className="app__body">
        <section className="app__editor-pane">
          <Editor
            height="100%"
            language={language.monacoId}
            value={sourceCode}
            onChange={(value) => setSourceCode(value ?? '')}
            theme="vs-dark"
            options={{
              fontFamily: 'IBM Plex Mono',
              fontSize: 14,
              minimap: { enabled: false },
              padding: { top: 16 },
              automaticLayout: true,
              scrollBeyondLastLine: false
            }}
          />

          <div className="app__stdin">
            <label className="app__stdin-label" htmlFor="stdin">
              stdin
            </label>
            <textarea
              id="stdin"
              className="app__stdin-input"
              value={stdin}
              onChange={(e) => setStdin(e.target.value)}
              placeholder="Input your program reads via stdin, if any"
              disabled={isRunning}
            />
          </div>
        </section>

        <section className="app__output-pane">
          <OutputPanel submission={submission} error={requestError} />
        </section>
      </main>
    </div>
  );
}
