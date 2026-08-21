import { useMemo } from "react";
import {
  ACTIVE_LIKE_POLICY_STATUSES,
  MAX_PENDING_PAYMENT_POLICIES,
  PRODUCT_POLICY_LIMITS,
} from "../utils/constants";
import { formatLabel } from "../utils/formatters";

// Computes purchase eligibility for a customer given existing policies
export function useEligibility(myPolicies = []) {
  const activeLikePolicies = useMemo(
    () => (myPolicies || []).filter((p) => ACTIVE_LIKE_POLICY_STATUSES.includes(p.status)),
    [myPolicies]
  );

  const pendingPaymentCount = useMemo(
    () => (myPolicies || []).filter((p) => p.status === "PENDING_PAYMENT").length,
    [myPolicies]
  );

  const getPlanEligibility = (plan) => {
    if (!plan) return { allowed: false, reason: "Invalid plan.", tone: "blocked" };

    const duplicatePlan =
      plan.productType === "MOTOR"
        ? null
        : activeLikePolicies.find((policy) => String(policy.planId) === String(plan.planId));

    if (duplicatePlan) {
      return {
        allowed: false,
        reason: `You already have this plan ${
          duplicatePlan.status === "ACTIVE" ? "active" : "awaiting payment"
        }`,
        tone: "blocked",
      };
    }

    if (pendingPaymentCount >= MAX_PENDING_PAYMENT_POLICIES) {
      return {
        allowed: false,
        reason: `You already have ${pendingPaymentCount} pending payments. Complete them before buying another plan.`,
        tone: "blocked",
      };
    }

    const productLimit = PRODUCT_POLICY_LIMITS[plan.productType];
    if (productLimit) {
      const sameProductCount = activeLikePolicies.filter(
        (policy) => policy.productType === plan.productType
      ).length;
      if (sameProductCount >= productLimit) {
        return {
          allowed: false,
          reason: `Standard customers can keep up to ${productLimit} active/pending ${formatLabel(
            plan.productType
          )} ${productLimit === 1 ? "policy" : "policies"}. Contact support for additional coverage.`,
          tone: "blocked",
        };
      }
    }

    return { allowed: true, reason: "Eligible for purchase under standard customer limits.", tone: "allowed" };
  };

  return { getPlanEligibility, activeLikePolicies, pendingPaymentCount };
}
