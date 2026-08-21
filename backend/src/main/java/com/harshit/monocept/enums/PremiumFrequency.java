package com.harshit.monocept.enums;

import java.math.BigDecimal;

public enum PremiumFrequency {


	ANNUAL(1, new BigDecimal("-0.02"), 30), HALF_YEARLY(2, new BigDecimal("-0.01"), 30),
	QUARTERLY(4, BigDecimal.ZERO, 30), MONTHLY(12, new BigDecimal("0.03"), 15);

	private final int installmentsPerYear;
	private final BigDecimal loadingFactor;
	private final int gracePeriodDays;

	PremiumFrequency(int installmentsPerYear, BigDecimal loadingFactor, int gracePeriodDays) {
		this.installmentsPerYear = installmentsPerYear;
		this.loadingFactor = loadingFactor;
		this.gracePeriodDays = gracePeriodDays;
	}

	public int getInstallmentsPerYear() {
		return installmentsPerYear;
	}

	public BigDecimal getLoadingFactor() {
		return loadingFactor;
	}

	public int getGracePeriodDays() {
		return gracePeriodDays;
	}

	public int getMonthsPerInstallment() {
		return 12 / installmentsPerYear;
	}
}