export default function ChipToggle({ selected, onToggle, children, title }) {
  return (
    <button
      type="button"
      className={`chip ${selected ? "selected" : ""}`}
      onClick={onToggle}
      aria-pressed={selected}
      title={title}
    >
      {children}
    </button>
  );
}
