import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { planApi } from "../../api/planApi";
import { policyApi } from "../../api/policyApi";
import { useFetch } from "../../hooks/useFetch";
import Loader from "../../components/common/Loader";
import Alert from "../../components/common/Alert";
import Button from "../../components/common/Button";
import Pagination from "../../components/common/Pagination";
import EmptyState from "../../components/common/EmptyState";
import { formatCurrency, formatLabel, formatDate } from "../../utils/formatters";
import MarketHero from "../../components/marketplace/MarketHero";
import EligibilityNote from "../../components/marketplace/EligibilityNote";
import { useEligibility } from "../../hooks/useEligibility";
import { PLAN_IMAGES } from "./browse/constants";
import { TODAY_STR } from "../../utils/dates";

export default function BrowsePlans() {
  const navigate = useNavigate();

  // Paging + messages
  const [page, setPage] = useState(0);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");

  // Plans list
  const { data, loading, error: fetchError } = useFetch(
    () => planApi.getActive({ page, size: 9 }),
    [page]
  );

  // Customer policies (for eligibility)
  const [myPolicies, setMyPolicies] = useState([]);
  const [policiesLoading, setPoliciesLoading] = useState(true);
  useEffect(() => {
    let ignore = false;
    setPoliciesLoading(true);
    policyApi
      .getMyPolicies({ page: 0, size: 100 })
      .then((res) => {
        if (!ignore) setMyPolicies(res?.data?.data?.content || []);
      })
      .catch(() => {
        if (!ignore) setError("Could not verify your existing policies. Please try again.");
      })
      .finally(() => {
        if (!ignore) setPoliciesLoading(false);
      });
    return () => {
      ignore = true;
    };
  }, []);

  const { getPlanEligibility } = useEligibility(myPolicies);

  if (loading) return <Loader label="Loading plans..." />;

  const totalPlans = data?.totalElements ?? data?.content?.length ?? 0;

  return (
    <div className="marketplace-page">
      <MarketHero total={totalPlans} />

      <Alert type="error" message={error || fetchError} onClose={() => setError("")} />
      <Alert type="success" message={success} onClose={() => setSuccess("")} />

      {data?.content?.length === 0 ? (
        <EmptyState message="No active plans available right now." />
      ) : (
        <div className="plan-grid">
          {data?.content?.map((plan) => {
            const imgUrl = PLAN_IMAGES[plan.productType] || PLAN_IMAGES.DEFAULT;
            const eligibility = policiesLoading
              ? { allowed: false, reason: "Checking your existing policies...", tone: "neutral" }
              : getPlanEligibility(plan);
            const isTravel = plan.productType === "TRAVEL";

            return (
              <div key={plan.planId} className="plan-card">
                <div className="plan-card-image" style={{ backgroundImage: `url('${imgUrl}')` }}>
                  <span className="plan-card-type">{formatLabel(plan.productType)}</span>
                </div>

                <div className="plan-card-body enhanced-plan-body">
                  <div className="plan-card-title-row">
                    <div>
                      <p className="plan-card-name">{plan.planName}</p>
                      <p className="plan-card-product">{plan.productName}</p>
                    </div>
                    <span className="plan-score-pill">Popular</span>
                  </div>

                  <div className="plan-figures">
                    <div>
                      <span className="figure-label">Coverage</span>
                      <span className="figure-value">{formatCurrency(plan.coverageAmount)}</span>
                    </div>
                    <div>
                      <span className="figure-label">Premium</span>
                      <span className="figure-value">
                        {formatCurrency(plan.premiumAmount)}
                        {plan.premiumType === "ANNUAL" ? "/year" : ""}
                      </span>
                    </div>
                  </div>

                  <div style={{ marginTop: "0.5rem" }}>
                    {isTravel ? (
                      <p className="plan-meta" style={{ margin: 0 }}>
                        We’ll collect trip dates and travellers on the next step.
                      </p>
                    ) : (
                      <p className="plan-meta" style={{ margin: 0 }}>
                        📅 Your policy will start today — {formatDate(TODAY_STR)}
                      </p>
                    )}
                  </div>

                  <p className="plan-meta">
                    {plan.premiumType === "ANNUAL" ? "Annual Premium" : "One Time Payment"}
                    &nbsp;·&nbsp;
                    {plan.durationYears} year{plan.durationYears > 1 ? "s" : ""}
                  </p>
                  <div className="plan-benefit-list">
                    <span>✓ Cashless-ready digital purchase</span>
                    <span>✓ Transparent premium and duration</span>
                    <span>✓ Eligibility checked instantly</span>
                  </div>
                  <p className="plan-terms">{plan.termsAndConditions}</p>

                  <div className="plan-card-footer">
                    <EligibilityNote tone={eligibility.tone}>{eligibility.reason}</EligibilityNote>
                    <Button
                      fullWidth
                      disabled={!eligibility.allowed || policiesLoading}
                      onClick={() => navigate(`/customer/purchase/${plan.planId}`, { state: { plan } })}
                    >
                      {eligibility.allowed ? "Buy Now" : "Not Eligible"}
                    </Button>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      )}

      <Pagination pageData={data} onPageChange={setPage} />
    </div>
  );
}
