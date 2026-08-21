import { useEffect, useMemo, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { planApi } from "../../api/planApi";
import { policyApi } from "../../api/policyApi";
import { customerApi } from "../../api/customerApi";
import Loader from "../../components/common/Loader";
import Alert from "../../components/common/Alert";
import Button from "../../components/common/Button";
import { formatCurrency, formatLabel } from "../../utils/formatters";
import { calcInstallment, frequencyUnit } from "../../utils/frequency";
import PurchaseHeader from "./purchase/PurchaseHeader";
import PurchaseSummary from "./purchase/PurchaseSummary";
import SegmentedControl from "../../components/common/SegmentedControl";
import MotorForm from "../../components/purchase/MotorForm";
import TravelForm from "../../components/purchase/TravelForm";
import HealthForm from "../../components/purchase/HealthForm";
import LifeForm from "../../components/purchase/LifeForm";
import { clampInt, ensureLength } from "../../utils/validators";
import { TODAY_STR } from "../../utils/dates";
import { CURRENT_YEAR } from "./purchase/constants";
import { useHealthEstimator } from "./purchase/useHealthEstimator";
import { validate } from "./purchase/validate";
import { buildPayload } from "./purchase/payload";
import { Car, Plane, HeartPulse, Shield, CalendarClock } from "lucide-react";

// constants and helpers moved to shared modules

export default function PurchasePlan() {
  const { planId } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const [loading, setLoading] = useState(true);
  const [plan, setPlan] = useState(null);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [purchasing, setPurchasing] = useState(false);
  const [prefilledNominee, setPrefilledNominee] = useState(false);

  // Shared state
  const [frequency, setFrequency] = useState("");

  // Product-specific states
  const [motor, setMotor] = useState({ regNo: "", make: "", model: "", year: "" });
  const [travel, setTravel] = useState({ start: TODAY_STR, end: "", adults: "", children: "" });
  const [health, setHealth] = useState({ coverType: "INDIVIDUAL", insuredAge: "", adultCount: "", childCount: "", adultAges: [], childAges: [], preConds: [], preCondsSince: {} });
  const [life, setLife] = useState({ dob: "", smoker: "", occupationRisk: "", nominees: [{ name: "", relationship: "SPOUSE", sharePct: 100, dob: "" }], consentTruth: false, consentTnc: false });

  // Errors
  const [formErr, setFormErr] = useState("");

  // UI helpers
  const ProductIcon = useMemo(() => {
    if (!plan) return Shield;
    switch (plan.productType) {
      case "MOTOR":
        return Car;
      case "TRAVEL":
        return Plane;
      case "HEALTH":
        return HeartPulse;
      case "LIFE":
      default:
        return Shield;
    }
  }, [plan]);

  const premiumPerInstallment = useMemo(() => {
    if (!plan) return null;
    const annual = Number(plan.premiumAmount || 0);
    if (plan.premiumType !== "ANNUAL") return annual; // one-time, no frequency loading
    if (!frequency) return null;
    return calcInstallment(annual, frequency);
  }, [plan, frequency]);

  const premiumInstallmentLabel = useMemo(() => {
    if (!plan) return "";
    if (plan.premiumType !== "ANNUAL") return `${formatCurrency(plan.premiumAmount)}`;
    if (!frequency || premiumPerInstallment == null)
      return `${formatCurrency(plan.premiumAmount)} / year`;
    const unit = frequencyUnit(frequency);
    return `${formatCurrency(premiumPerInstallment)} / ${unit}`;
  }, [plan, frequency, premiumPerInstallment]);

  // Estimated premium calculation moved to custom hook
  const { estimatedHealthAnnual, estimatedHealthInstallmentLabel } = useHealthEstimator(plan, health, frequency);

  useEffect(() => {
    let ignore = false;
    const fromState = location.state?.plan;
    if (fromState) {
      setPlan(fromState);
      setLoading(false);
      return;
    }
    setLoading(true);
    // Try direct getById first; fallback to getActive and find
    planApi
      .getById(planId)
      .then((res) => {
        if (!ignore) setPlan(res.data.data);
      })
      .catch(() => {
        return planApi.getActive({ page: 0, size: 1000 }).then((res) => {
          const list = res?.data?.data?.content || res?.data?.data || [];
          const found = (list || []).find((p) => String(p.planId) === String(planId));
          if (!ignore) {
            if (found) setPlan(found);
            else setError("Plan not found.");
          }
        });
      })
      .finally(() => !ignore && setLoading(false));
    return () => {
      ignore = true;
    };
  }, [planId, location.state]);

  // Prefill LIFE-specific details from profile (editable fields)
  useEffect(() => {
    let ignore = false;
    if (!plan || plan.productType !== "LIFE") return;
    // Only prefill if fields are empty; do not override user input
    customerApi
      .getMyProfile()
      .then((res) => {
        if (ignore) return;
        const profile = res?.data?.data || {};
        const profName = profile.nomineeName?.trim();
        const profRel = profile.nomineeRelation || "";
        const profDob = profile.dateOfBirth || "";
        setLife((curr) => {
          const nom0 = curr?.nominees?.[0] || {};
          let changed = false;
          const next = { ...curr };
          // Prefill nominee name/relationship if empty
          if ((!nom0.name || nom0.name.trim() === "") && profName) {
            next.nominees = [{ ...nom0, name: profName, relationship: nom0.relationship || profRel || "OTHER", sharePct: 100, dob: nom0.dob || "" }];
            changed = true;
          } else if ((!nom0.relationship || nom0.relationship === "") && profRel) {
            next.nominees = [{ ...nom0, relationship: profRel, sharePct: nom0.sharePct ?? 100, name: nom0.name || "", dob: nom0.dob || "" }];
            changed = true;
          } else if (nom0.sharePct == null) {
            next.nominees = [{ ...nom0, sharePct: 100 }];
            changed = true;
          }
          // Prefill applicant DOB for life if empty
          if ((!curr?.dob || curr.dob === "") && profDob) {
            next.dob = profDob;
            changed = true;
          }
          if (changed) setPrefilledNominee(true);
          return changed ? next : curr;
        });
      })
      .catch(() => {});
    return () => {
      ignore = true;
    };
  }, [plan]);

  // helpers moved to utils/validators

  // Validations per product
  const runValidate = () => validate(plan, frequency, { motor, travel, health, life });

  const onSubmit = async () => {
    setError("");
    const vmsg = runValidate();
    if (vmsg) {
      setFormErr(vmsg);
      return;
    }
    setPurchasing(true);
    try {
      const payload = buildPayload(plan, frequency, { planId, motor, travel, health, life });
      await policyApi.purchase(payload);
      setSuccess("Policy purchased. Complete payment from My Policies.");
      setTimeout(() => navigate("/customer/policies"), 600);
    } catch (e) {
      setError(typeof e?.response?.data?.message === "string" ? e.response.data.message : "Purchase failed.");
    } finally {
      setPurchasing(false);
    }
  };

  // UI components imported

  const renderForm = () => {
    if (!plan) return null;
    const type = plan.productType;
    return (
      <div className="purchase-grid">
        {/* Left: form column */}
        <div className="purchase-content">
          <PurchaseHeader
            Icon={ProductIcon}
            planName={plan.planName}
            productType={type}
            coverageAmount={plan.coverageAmount}
            premiumLabel={premiumInstallmentLabel}
            estimatedLabel={plan.productType === "HEALTH" ? estimatedHealthInstallmentLabel : ""}
          />

          {plan.premiumType === "ANNUAL" && (
            <div className="card">
              <div className="card-body">
                <label className="form-label">Payment frequency</label>
                <SegmentedControl
                  value={frequency}
                  onChange={setFrequency}
                  options={[
                    { label: "Monthly", value: "MONTHLY" },
                    { label: "Quarterly", value: "QUARTERLY" },
                    { label: "Half-yearly", value: "HALF_YEARLY" },
                    { label: "Annual", value: "ANNUAL" },
                  ]}
                />
                <p className="help">
                  Monthly +3%, Quarterly 0%, Half-yearly -1%, Annual -2% (server rules apply).
                </p>
              </div>
            </div>
          )}
        {type === "MOTOR" && <MotorForm value={motor} onChange={setMotor} />}
        {type === "TRAVEL" && <TravelForm value={travel} onChange={setTravel} />}
        {type === "HEALTH" && <HealthForm value={health} onChange={setHealth} />}
        {type === "LIFE" && <LifeForm value={life} onChange={setLife} prefilledNote={prefilledNominee} />}
        {formErr && <Alert type="error" message={formErr} onClose={() => setFormErr("")} />}
        {/* Close form column before starting sticky summary */}
        </div>
        {/* Right: sticky summary */}
        <PurchaseSummary
          plan={plan}
          estimatedHealthAnnual={estimatedHealthAnnual}
          estimatedHealthInstallmentLabel={estimatedHealthInstallmentLabel}
          premiumInstallmentLabel={premiumInstallmentLabel}
          formErr={formErr}
          error={error}
          success={success}
          onCloseFormErr={() => setFormErr("")}
          onCloseError={() => setError("")}
          onCloseSuccess={() => setSuccess("")}
          onBack={() => navigate(-1)}
          onSubmit={onSubmit}
          purchasing={purchasing}
        />
      </div>
    );
  };

  if (loading) return <Loader label="Loading plan..." />;
  if (!plan) return <Alert type="error" message="Plan not found." />;

  return (
    <div className="marketplace-page">
      {renderForm()}
    </div>
  );
}
