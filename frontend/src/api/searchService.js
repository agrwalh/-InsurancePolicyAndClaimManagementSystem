import { policyApi } from "./policyApi";
import { claimApi } from "./claimApi";

const normalizePolicy = (p) => ({
  type: "policy",
  id: p.policyId ?? p.id,
  number: p.policyNumber,
  title: p.planName ?? "Policy",
  subtitle: `${(p.productType || "").toLowerCase()} • ${(p.premiumType || "").toLowerCase()}`,
  status: p.status,
});

const normalizeClaim = (c) => ({
  type: "claim",
  id: c.claimId ?? c.id,
  number: c.claimNumber,
  title: c.claimReason || "Claim",
  subtitle: `${(c.claimStatus || "").toLowerCase()}`,
  status: c.claimStatus,
});

function includesCI(haystack, needle) {
  if (!haystack || !needle) return false;
  return String(haystack).toLowerCase().includes(String(needle).toLowerCase());
}

export async function searchEverything(query, role) {
  const q = (query || "").trim();
  if (q.length < 2) return { policies: [], claims: [] };

  const isNumeric = /^\d+$/.test(q);
  const looksPolicyNo = /^pol[-_]/i.test(q);
  const looksClaimNo = /^clm[-_]/i.test(q);

  const results = { policies: [], claims: [] };

  // 1) Try direct ID fetches when q is numeric
  if (isNumeric) {
    await Promise.allSettled([
      policyApi
        .getById(q)
        .then((res) => {
          const p = res?.data?.data;
          if (p) results.policies.push(normalizePolicy(p));
        })
        .catch(() => {}),
      claimApi
        .getById(q)
        .then((res) => {
          const c = res?.data?.data;
          if (c) results.claims.push(normalizeClaim(c));
        })
        .catch(() => {}),
    ]);
  }

  // 2) Fetch first page lists and client-filter by number/title (MVP)
  const policyListFetcher = role === "CUSTOMER" ? policyApi.getMyPolicies : policyApi.getAll;
  const claimListFetcher = role === "CUSTOMER" ? claimApi.getMyClaims : claimApi.getAll;

  try {
    const [pRes, cRes] = await Promise.all([
      policyListFetcher({ page: 0, size: 50 }),
      claimListFetcher({ page: 0, size: 50 }),
    ]);
    const pItems = pRes?.data?.data?.content || pRes?.data?.data || [];
    const cItems = cRes?.data?.data?.content || cRes?.data?.data || [];

    const pFiltered = pItems.filter(
      (p) =>
        includesCI(p.policyNumber, q) ||
        includesCI(p.planName, q) ||
        (looksPolicyNo && includesCI(p.policyNumber, q)) ||
        (isNumeric && String(p.policyId ?? p.id) === q),
    );
    const cFiltered = cItems.filter(
      (c) =>
        includesCI(c.claimNumber, q) ||
        includesCI(c.claimReason, q) ||
        (looksClaimNo && includesCI(c.claimNumber, q)) ||
        (isNumeric && String(c.claimId ?? c.id) === q),
    );

    // Merge unique by id
    const pushUnique = (arr, obj, key = "id") => {
      if (!arr.some((x) => String(x[key]) === String(obj[key]))) arr.push(obj);
    };

    pFiltered.map(normalizePolicy).forEach((x) => pushUnique(results.policies, x));
    cFiltered.map(normalizeClaim).forEach((x) => pushUnique(results.claims, x));
  } catch (e) {
    // ignore, return whatever we have
  }

  // Cap suggestions to 5 each for dropdowns; caller can paginate on /search
  return {
    policies: results.policies.slice(0, 5),
    claims: results.claims.slice(0, 5),
  };
}
