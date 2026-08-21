import { CalendarClock } from "lucide-react";
import { formatCurrency, formatLabel } from "../../../utils/formatters";

export default function PurchaseHeader({
  Icon,
  planName,
  productType,
  coverageAmount,
  premiumLabel,
  estimatedLabel,
}) {
  return (
    <div className="purchase-header">
      <div className="purchase-header-main">
        <span className="purchase-icon" aria-hidden>
          <Icon size={20} />
        </span>
        <div>
          <h2 className="purchase-title">{planName}</h2>
          <div className="purchase-sub">
            {formatLabel(productType)} • {formatCurrency(coverageAmount)} cover
          </div>
        </div>
      </div>
      <div className="purchase-premium">
        <CalendarClock size={16} />
        {estimatedLabel ? (
          <>
            <span>≈ {estimatedLabel}</span>
            <span className="chip" style={{ marginLeft: 8 }}>Estimated</span>
          </>
        ) : (
          <span>{premiumLabel}</span>
        )}
      </div>
    </div>
  );
}
