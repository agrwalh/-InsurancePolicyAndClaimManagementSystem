import { TODAY_STR } from "../../../utils/dates";
import { clampInt } from "../../../utils/validators";

export function buildPayload(plan, frequency, { planId, motor, travel, health, life }) {
  const payload = { planId: Number(planId), startDate: plan.productType === "TRAVEL" ? (travel?.start) : TODAY_STR };
  if (plan.premiumType === "ANNUAL") payload.premiumFrequency = frequency;

  switch (plan.productType) {
    case "MOTOR": {
      const v = motor || {};
      payload.vehicleRegistrationNumber = v.regNo?.trim()?.toUpperCase();
      payload.vehicleMake = v.make?.trim();
      payload.vehicleModel = v.model?.trim();
      payload.vehicleManufactureYear = Number(v.year);
      break;
    }
    case "TRAVEL": {
      const v = travel || {};
      payload.tripEndDate = v.end;
      payload.travellersAdultCount = Number(clampInt(v.adults, 1, 9));
      payload.travellersChildCount = Number(clampInt(v.children || 0, 0, 9));
      break;
    }
    case "HEALTH": {
      const h = health || {};
      payload.healthCoverType = h.coverType || "INDIVIDUAL";
      if ((h.coverType || "INDIVIDUAL") === "INDIVIDUAL") {
        payload.healthInsuredAge = Number(h.insuredAge);
      } else {
        payload.healthAdultCount = Number(h.adultCount || 0);
        payload.healthChildCount = Number(h.childCount || 0);
        payload.healthAdultAges = (h.adultAges || []).map((x) => Number(x));
        payload.healthChildAges = (h.childAges || []).map((x) => Number(x));
      }
      payload.healthPreExistingConditions = Array.isArray(h.preConds) ? h.preConds : [];
      payload.healthPreExistingSinceYears = Array.isArray(h.preConds)
        ? h.preConds.map((c) => {
            const v = h.preCondsSince?.[c];
            return v === "" || v === undefined || v === null ? null : Number(v);
          })
        : [];
      payload.healthHasPreExisting = payload.healthPreExistingConditions.length > 0;
      break;
    }
    case "LIFE": {
      const l = life || {};
      payload.lifeDob = l.dob;
      payload.lifeSmoker = l.smoker === true || l.smoker === "true";
      payload.lifeNominees = [
        {
          name: l.nominees?.[0]?.name?.trim(),
          relationship: l.nominees?.[0]?.relationship || "OTHER",
          sharePct: Number(l.nominees?.[0]?.sharePct || 0),
          ...(l.nominees?.[0]?.dob ? { dob: l.nominees[0].dob } : {}),
        },
      ];
      break;
    }
    default:
      break;
  }

  return payload;
}
