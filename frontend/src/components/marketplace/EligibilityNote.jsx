export default function EligibilityNote({ tone = "neutral", children }) {
  const icon = tone === "blocked" ? "⚠️" : tone === "allowed" ? "✅" : "ℹ️";
  return (
    <p className={`eligibility-note eligibility-${tone}`}>
      {icon} {children}
    </p>
  );
}
