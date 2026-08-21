import { TODAY_STR } from "../../../utils/dates";
import { ensureLength } from "../../../utils/validators";
import { CURRENT_YEAR, HEALTH_CONDITION_OPTIONS } from "./constants";

export function validate(plan, frequency, { motor, travel, health, life }) {
  if (!plan) return "";
  if (plan.premiumType === "ANNUAL" && !frequency) return "Please choose a payment frequency.";

  const type = plan.productType;
  if (type === "MOTOR") {
    const v = motor || {};
    if (!v.regNo || !v.make || !v.model || !v.year) return "Fill all vehicle details.";
    const y = Number(v.year);
    if (Number.isNaN(y) || y > CURRENT_YEAR || CURRENT_YEAR - y > 15)
      return "Invalid manufacture year.";
    return "";
  }

  if (type === "TRAVEL") {
    const v = travel || {};
    const s = new Date(v.start);
    const e = new Date(v.end);
    if (!v.end) return "Please choose trip end date.";
    if (e < s) return "End date cannot be before start date.";
    const days = Math.floor((e - s) / (1000 * 60 * 60 * 24)) + 1;
    if (days < 1 || days > 180) return "Trip duration must be 1-180 days.";
    const a = Math.max(1, Math.min(9, Number(v.adults || 0)));
    const c = Math.max(0, Math.min(9, Number(v.children || 0)));
    if (a + c < 1) return "At least one traveller is required.";
    return "";
  }

  if (type === "HEALTH") {
    const v = health || {};
    const ct = v.coverType || "INDIVIDUAL";
    if (ct !== "INDIVIDUAL" && ct !== "FLOATER") return "Choose cover type.";
    // Validate pre-existing
    for (const code of v.preConds || []) {
      if (!HEALTH_CONDITION_OPTIONS.includes(code)) return "Invalid pre-existing code.";
      const yr = v.preCondsSince?.[code];
      if (yr !== undefined && yr !== null && yr !== "") {
        const y = Number(yr);
        if (Number.isNaN(y) || y < CURRENT_YEAR - 80 || y > CURRENT_YEAR)
          return "Since year must be valid.";
      }
    }
    if (ct === "INDIVIDUAL") {
      const age = Number(v.insuredAge);
      if (!age && age !== 0) return "Enter age.";
      if (age < 18 || age > 65) return "Age must be 18–65.";
      return "";
    }
    // Floater
    const aCount = Number(v.adultCount || 0);
    const cCount = Number(v.childCount || 0);
    if (aCount < 1 || aCount > 2) return "Adults must be 1 or 2.";
    if (cCount < 0 || cCount > 4) return "Children must be 0–4.";
    const aAges = ensureLength(v.adultAges, aCount);
    const cAges = ensureLength(v.childAges, cCount);
    for (const ag of aAges) {
      const n = Number(ag);
      if (Number.isNaN(n) || n < 18 || n > 65) return "Each adult age must be 18–65.";
    }
    for (const cg of cAges) {
      const n = Number(cg);
      if (Number.isNaN(n) || n < 0 || n > 25) return "Each child age must be 0–25.";
    }
    return "";
  }

  if (type === "LIFE") {
    const v = life || {};
    if (!v.dob) return "Enter DOB.";
    const dob = new Date(v.dob);
    const today = new Date(TODAY_STR);
    let age = today.getFullYear() - dob.getFullYear();
    const m = today.getMonth() - dob.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < dob.getDate())) age--;
    if (age < 18 || age > 65) return "Age must be 18–65.";
    if (v.smoker !== true && v.smoker !== false && v.smoker !== "true" && v.smoker !== "false")
      return "Select smoker.";
    const n = v.nominees?.[0] || {};
    if (!n.name || !n.relationship) return "Nominee name & relationship required.";
    const s = Number(n.sharePct || 0);
    if (s !== 100) return "Nominee share must total 100%.";
    if (!v.consentTruth || !v.consentTnc) return "Accept consents.";
    return "";
  }

  return "";
}
