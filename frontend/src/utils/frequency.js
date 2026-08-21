

export const FREQUENCY_LOADING = {
  MONTHLY: 0.03,
  QUARTERLY: 0,
  HALF_YEARLY: -0.01,
  ANNUAL: -0.02,
};

export const INSTALLMENTS_PER_YEAR = {
  MONTHLY: 12,
  QUARTERLY: 4,
  HALF_YEARLY: 2,
  ANNUAL: 1,
};

export function applyFrequencyLoading(annualAmount, frequency) {
  const load = FREQUENCY_LOADING[frequency] ?? 0;
  const base = Number(annualAmount || 0);
  return base * (1 + load);
}

export function calcInstallment(annualAmount, frequency) {
  const loadedAnnual = applyFrequencyLoading(annualAmount, frequency);
  const n = INSTALLMENTS_PER_YEAR[frequency] ?? 1;
  return loadedAnnual / n;
}

export function frequencyUnit(frequency) {
  switch (frequency) {
    case "MONTHLY":
      return "month";
    case "QUARTERLY":
      return "quarter";
    case "HALF_YEARLY":
      return "half-year";
    case "ANNUAL":
    default:
      return "year";
  }
}
