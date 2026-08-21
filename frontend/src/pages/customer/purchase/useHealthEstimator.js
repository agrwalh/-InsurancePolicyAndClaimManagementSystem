import { useMemo } from "react";
import { formatCurrency } from "../../../utils/formatters";
import { calcInstallment, frequencyUnit, applyFrequencyLoading } from "../../../utils/frequency";
import { CURRENT_YEAR } from "./constants";

export function useHealthEstimator(plan, health, frequency) {
  const estimatedHealthAnnual = useMemo(() => {
    if (!plan || plan.productType !== "HEALTH") return null;
    const base = Number(plan.premiumAmount || 0);
    const v = health || {};

    let age = 30;
    if ((v.coverType || "INDIVIDUAL") === "INDIVIDUAL") {
      const n = Number(v.insuredAge);
      if (!Number.isNaN(n) && n > 0) age = n;
    } else {
      const aAges = (v.adultAges || []).map((x) => Number(x)).filter((n) => !Number.isNaN(n) && n > 0);
      if (aAges.length > 0) age = Math.max(...aAges);
    }

    let ageFactor = 0;
    if (age >= 56) ageFactor = 0.30;
    else if (age >= 46) ageFactor = 0.15;
    else if (age >= 36) ageFactor = 0.07;

    const LOAD = {
      DIABETES: 0.12,
      HYPERTENSION: 0.08,
      ASTHMA_COPD: 0.10,
      THYROID: 0.04,
      HEART_DISEASE: 0.25,
      KIDNEY_LIVER: 0.20,
      OTHER: 0.05,
    };
    let condSum = 0;
    const codes = Array.isArray(v.preConds) ? v.preConds : [];
    for (const code of codes) {
      let l = LOAD[code] || 0;
      const since = v.preCondsSince?.[code];
      if (since !== undefined && since !== null && since !== "") {
        const y = Number(since);
        if (!Number.isNaN(y)) {
          const yrs = CURRENT_YEAR - y;
          if (yrs <= 3) l += (LOAD[code] || 0) * 0.5;
          else if (yrs >= 10) l = Math.max(0, l - (LOAD[code] || 0) * 0.25);
        }
      }
      condSum += l;
    }
    condSum = Math.min(condSum, 0.60);

    const childAdj = Math.min(Math.max(Number(v.childCount || 0), 0) * 0.02, 0.08);
    let totalLoad = ageFactor + condSum + childAdj;
    totalLoad = Math.max(0, Math.min(totalLoad, 0.80));

    const adjusted = Math.round((base * (1 + totalLoad)) / 10) * 10;
    return adjusted;
  }, [plan, health]);

  const estimatedHealthInstallmentLabel = useMemo(() => {
    if (!plan || plan.productType !== "HEALTH" || estimatedHealthAnnual == null) return "";
    if (plan.premiumType !== "ANNUAL") return `${formatCurrency(estimatedHealthAnnual)}`;
    if (!frequency) return `${formatCurrency(estimatedHealthAnnual)} / year`;
    const perInst = calcInstallment(estimatedHealthAnnual, frequency);
    const unit = frequencyUnit(frequency);
    return `${formatCurrency(perInst)} / ${unit}`;
  }, [plan, frequency, estimatedHealthAnnual]);

  return { estimatedHealthAnnual, estimatedHealthInstallmentLabel };
}
