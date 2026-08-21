export default function SegmentedControl({ value, onChange, options, ariaLabel = "Options" }) {
  return (
    <div className="segmented" role="tablist" aria-label={ariaLabel}>
      {options.map((opt) => (
        <button
          key={opt.value}
          type="button"
          role="tab"
          className={`segmented-option ${value === opt.value ? "active" : ""}`}
          aria-selected={value === opt.value}
          onClick={() => onChange(opt.value)}
        >
          {opt.label}
        </button>
      ))}
    </div>
  );
}
