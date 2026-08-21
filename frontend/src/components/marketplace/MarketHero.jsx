export default function MarketHero({ total = 0 }) {
  return (
    <div className="marketplace-hero">
      <div>
        <span className="eyebrow">Plan Marketplace</span>
        <h1>Choose protection that feels simple, clear, and trustworthy.</h1>
        <p>
          Compare coverage, premium style, eligibility, and policy duration before you buy.
          No confusing steps — just choose the right plan.
        </p>
      </div>
      <div className="marketplace-trust-card">
        <strong>{total}</strong>
        <span>available plans</span>
        <p>Eligibility is checked against your existing policies before purchase.</p>
      </div>
    </div>
  );
}
