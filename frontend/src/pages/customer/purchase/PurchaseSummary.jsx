import Alert from "../../../components/common/Alert";
import Button from "../../../components/common/Button";
import { formatCurrency } from "../../../utils/formatters";

export default function PurchaseSummary({
  plan,
  estimatedHealthAnnual,
  estimatedHealthInstallmentLabel,
  premiumInstallmentLabel,
  formErr,
  error,
  success,
  onCloseFormErr,
  onCloseError,
  onCloseSuccess,
  onBack,
  onSubmit,
  purchasing,
}) {
  return (
    <aside className="purchase-summary">
      <div className="card summary-card">
        <div className="card-body">
          <div className="summary-row">
            <span className="summary-label">Coverage</span>
            <span className="summary-value">{formatCurrency(plan.coverageAmount)}</span>
          </div>
          {plan.productType === "HEALTH" && estimatedHealthAnnual != null && (
            <div className="summary-row">
              <span className="summary-label">Estimated premium</span>
              <span className="summary-value">
                {plan.premiumType === "ANNUAL"
                  ? `${formatCurrency(estimatedHealthAnnual)} / year`
                  : formatCurrency(estimatedHealthAnnual)}
              </span>
            </div>
          )}
          <div className="summary-row">
            <span className="summary-label">Base premium</span>
            <span className="summary-value">
              {formatCurrency(plan.premiumAmount)}
              {plan.premiumType === "ANNUAL" ? " / year" : ""}
            </span>
          </div>
          {plan.premiumType === "ANNUAL" && (
            <div className="summary-row">
              <span className="summary-label">You pay</span>
              <span className="summary-value accent">
                {plan.productType === "HEALTH" && estimatedHealthInstallmentLabel
                  ? estimatedHealthInstallmentLabel
                  : premiumInstallmentLabel}
              </span>
            </div>
          )}

          {formErr && <Alert type="error" message={formErr} onClose={onCloseFormErr} />}
          {error && <Alert type="error" message={error} onClose={onCloseError} />}
          {success && <Alert type="success" message={success} onClose={onCloseSuccess} />}

          {plan.productType === "HEALTH" && (
            <p className="summary-fine" style={{ marginTop: 4 }}>
              Estimated premium is based on disclosures; final billing may reflect underwriting rules.
            </p>
          )}

          <div className="summary-actions">
            <Button onClick={onBack} disabled={purchasing} variant="secondary">Back</Button>
            <Button onClick={onSubmit} loading={purchasing}>Purchase</Button>
          </div>
          <p className="summary-fine">By continuing, you agree to our Terms & Privacy Policy.</p>
        </div>
      </div>
    </aside>
  );
}
