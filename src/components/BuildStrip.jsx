import './BuildStrip.css';

const STAGES = [
  { key: 'QUEUED', label: 'Queued' },
  { key: 'RUNNING', label: 'Running' },
  { key: 'COMPLETED', label: 'Done' }
];

// Maps a submission's status (plus its result verdict, once known) to
// how "far lit" the strip is, and what color the final segment takes.
function stageIndex(status) {
  return STAGES.findIndex((s) => s.key === status);
}

export default function BuildStrip({ status, verdict }) {
  const activeIndex = status ? stageIndex(status) : -1;

  const finalColor =
    status === 'COMPLETED'
      ? verdict === 'SUCCESS'
        ? 'var(--green)'
        : 'var(--red)'
      : 'var(--amber)';

  return (
    <div className="build-strip" role="status" aria-label={`Submission status: ${status ?? 'idle'}`}>
      {STAGES.map((stage, i) => {
        const isActive = i === activeIndex;
        const isPast = i < activeIndex;
        const isFuture = i > activeIndex;
        const isFinal = i === STAGES.length - 1 && status === 'COMPLETED';

        return (
          <div
            key={stage.key}
            className={[
              'build-strip__segment',
              isPast || isActive ? 'build-strip__segment--lit' : '',
              isActive && !isFinal ? 'build-strip__segment--pulsing' : '',
              isFuture ? 'build-strip__segment--dim' : ''
            ].join(' ')}
            style={isFinal ? { '--segment-color': finalColor } : undefined}
          >
            <span className="build-strip__dot" />
            <span className="build-strip__label">{stage.label}</span>
          </div>
        );
      })}
    </div>
  );
}
