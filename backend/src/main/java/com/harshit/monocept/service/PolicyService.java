package com.harshit.monocept.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harshit.monocept.dto.request.PolicyIssueRequest;
import com.harshit.monocept.dto.request.PolicyPurchaseRequest;
import com.harshit.monocept.dto.response.PolicyResponse;
import com.harshit.monocept.dto.response.HealthPreExistingDto;
import com.harshit.monocept.entity.Customer;
import com.harshit.monocept.entity.Policy;
import com.harshit.monocept.entity.PolicyPlan;
import com.harshit.monocept.entity.HealthPreExistingEntry;
import com.harshit.monocept.entity.User;
import com.harshit.monocept.enums.PolicyStatus;
import com.harshit.monocept.enums.PremiumFrequency;
import com.harshit.monocept.enums.PremiumType;
import com.harshit.monocept.enums.ProductType;
import com.harshit.monocept.enums.Role;
import com.harshit.monocept.exception.BusinessRuleException;
import com.harshit.monocept.exception.ResourceNotFoundException;
import com.harshit.monocept.repository.CustomerRepository;
import com.harshit.monocept.repository.PolicyPlanRepository;
import com.harshit.monocept.repository.PolicyRepository;
import com.harshit.monocept.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyService {

	private static final Logger log = LoggerFactory.getLogger(PolicyService.class);
	private static final Pattern VEHICLE_REG_PATTERN = Pattern.compile("^[A-Z]{2}[0-9]{1,2}[A-Z]{1,3}[0-9]{4}$");
	private static final int MAX_VEHICLE_AGE_YEARS = 15;
	private static final int MAX_TRAVEL_ADVANCE_DAYS = 90;

	private final PolicyRepository policyRepository;
	private final PolicyPlanRepository planRepository;
	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;

	private BigDecimal getDepreciationPercent(int vehicleAgeYears) {
		if (vehicleAgeYears <= 0)
			return new BigDecimal("0.05");
		if (vehicleAgeYears == 1)
			return new BigDecimal("0.15");
		if (vehicleAgeYears == 2)
			return new BigDecimal("0.20");
		if (vehicleAgeYears == 3)
			return new BigDecimal("0.30");
		if (vehicleAgeYears == 4)
			return new BigDecimal("0.40");

		return new BigDecimal("0.50");
	}

	private BigDecimal calculateIdv(BigDecimal baseVehicleValue, int vehicleAgeYears) {
		BigDecimal depreciationPct = getDepreciationPercent(vehicleAgeYears);
		BigDecimal depreciationAmount = baseVehicleValue.multiply(depreciationPct);

		return baseVehicleValue.subtract(depreciationAmount).setScale(2, RoundingMode.HALF_UP);
	}

	@Transactional
	public PolicyResponse purchasePolicy(PolicyPurchaseRequest req, String email) {
		log.info("Policy purchase attempt: email={}, planId={}", email, req.getPlanId());

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Customer customer = customerRepository.findByUserId(user.getId()).orElseThrow(() -> {
			log.warn("Policy purchase without profile: email={}", email);
			return new BusinessRuleException("Please complete your profile before purchasing a policy");
		});

		PolicyPlan plan = planRepository.findById(req.getPlanId())
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + req.getPlanId()));

		if (!plan.getIsActive()) {
			log.warn("Purchase attempt on inactive plan: planId={}", req.getPlanId());
			throw new BusinessRuleException("Cannot purchase an inactive plan");
		}

		if (!plan.getProduct().getIsActive()) {
			log.warn("Purchase attempt on inactive product: productId={}", plan.getProduct().getId());
			throw new BusinessRuleException("Cannot purchase plan of an inactive product");
		}
		List<PolicyStatus> activeLikeStatuses = List.of(PolicyStatus.PENDING_PAYMENT, PolicyStatus.ACTIVE,
				PolicyStatus.LAPSED);

		if (plan.getProduct().getProductType() == ProductType.MOTOR) {

			String regNo = req.getVehicleRegistrationNumber().trim().toUpperCase().replaceAll("\\s+", "");

			boolean vehicleAlreadyInsured = policyRepository.existsByVehicleRegistrationNumberAndStatusIn(regNo,
					activeLikeStatuses);

			if (vehicleAlreadyInsured) {
				throw new BusinessRuleException("This vehicle is already insured under an active or pending policy");
			}

		} else {

			boolean hasDuplicate = !policyRepository
					.findByCustomerIdAndPlanIdAndStatusIn(customer.getId(), plan.getId(), activeLikeStatuses).isEmpty();

			if (hasDuplicate) {
				throw new BusinessRuleException("You already have an active or pending policy for this plan");
			}
		}

		int pendingCount = policyRepository.countByCustomerIdAndStatus(customer.getId(), PolicyStatus.PENDING_PAYMENT);
		if (pendingCount >= 2) {
			throw new BusinessRuleException(
					"You already have " + pendingCount + " pending payments. Complete them before buying another plan");
		}

		int sameProductCount = policyRepository.countByCustomerIdAndPlanProductProductTypeAndStatusIn(customer.getId(),
				plan.getProduct().getProductType(), activeLikeStatuses);
		int limit = plan.getProduct().getProductType() == ProductType.LIFE ? 1
				: plan.getProduct().getProductType() == ProductType.HEALTH ? 2 : Integer.MAX_VALUE;
		if (sameProductCount >= limit) {
			throw new BusinessRuleException("You've reached the maximum number of active "
					+ plan.getProduct().getProductType() + " policies (" + limit + ") allowed for standard customers");
		}

		if (plan.getProduct().getProductType() == ProductType.TRAVEL) {
			LocalDate start = req.getStartDate();
			LocalDate tripEnd = req.getTripEndDate();
			if (tripEnd == null) {
				throw new BusinessRuleException("Trip end date is required for travel policies");
			}
			if (tripEnd.isBefore(start)) {
				throw new BusinessRuleException("Trip end date cannot be before the start date");
			}
			long durationDays = ChronoUnit.DAYS.between(start, tripEnd) + 1;
			if (durationDays < 1 || durationDays > 180) {
				throw new BusinessRuleException("Travel duration must be between 1 and 180 days");
			}

			Integer adults = req.getTravellersAdultCount();
			Integer children = req.getTravellersChildCount() == null ? 0 : req.getTravellersChildCount();
			if (adults == null || adults < 1 || adults > 9) {
				throw new BusinessRuleException("Adults count must be between 1 and 9 for travel policies");
			}
			if (children < 0 || children > 9) {
				throw new BusinessRuleException("Children count must be between 0 and 9 for travel policies");
			}
			if (adults + children < 1) {
				throw new BusinessRuleException("At least one traveller is required");
			}
		} else if (plan.getProduct().getProductType() == ProductType.HEALTH) {
			String coverType = req.getHealthCoverType();
			if (coverType == null || !(coverType.equals("INDIVIDUAL") || coverType.equals("FLOATER"))) {
				throw new BusinessRuleException("Cover type is required for health policies (Individual or Floater)");
			}

			List<String> conds = req.getHealthPreExistingConditions();
			List<Integer> years = req.getHealthPreExistingSinceYears();
			if (conds != null) {
				if (years != null && years.size() != conds.size()) {
					throw new BusinessRuleException("Pre-existing conditions and years size mismatch");
				}
				for (int i = 0; i < conds.size(); i++) {
					String code = conds.get(i);
					if (code == null) {
						throw new BusinessRuleException("Invalid pre-existing condition");
					}
					switch (code) {
					case "DIABETES":
					case "HYPERTENSION":
					case "ASTHMA_COPD":
					case "THYROID":
					case "HEART_DISEASE":
					case "KIDNEY_LIVER":
					case "OTHER":
						break;
					default:
						throw new BusinessRuleException("Invalid pre-existing condition: " + code);
					}
					if (years != null) {
						Integer yr = years.get(i);
						if (yr != null) {
							int y = yr;
							int min = Year.now().getValue() - 80;
							int max = Year.now().getValue();
							if (y < min || y > max) {
								throw new BusinessRuleException("Since year must be within last 80 years");
							}
						}
					}
				}
			}
			if (coverType.equals("INDIVIDUAL")) {
				Integer age = req.getHealthInsuredAge();
				if (age == null) {
					throw new BusinessRuleException("Age is required for Individual health cover");
				}
				if (age < 18 || age > 65) {
					throw new BusinessRuleException("Insured age must be between 18 and 65 years for health");
				}
			} else { // FLOATER
				Integer adultCount = req.getHealthAdultCount();
				Integer childCount = req.getHealthChildCount() == null ? 0 : req.getHealthChildCount();
				if (adultCount == null || adultCount < 1 || adultCount > 2) {
					throw new BusinessRuleException("For floater, adults must be 1 or 2");
				}
				if (childCount < 0 || childCount > 4) {
					throw new BusinessRuleException("For floater, children must be between 0 and 4");
				}
				List<Integer> adultAges = req.getHealthAdultAges();
				if (adultAges == null || adultAges.size() != adultCount) {
					throw new BusinessRuleException("Provide exactly " + adultCount + " adult ages");
				}
				for (Integer a : adultAges) {
					if (a == null || a < 18 || a > 65) {
						throw new BusinessRuleException("Each adult age must be between 18 and 65");
					}
				}
				if (childCount > 0) {
					List<Integer> childAges = req.getHealthChildAges();
					if (childAges == null || childAges.size() != childCount) {
						throw new BusinessRuleException("Provide exactly " + childCount + " child ages");
					}
					for (Integer c : childAges) {
						if (c == null || c < 0 || c > 25) {
							throw new BusinessRuleException("Each child age must be between 0 and 25");
						}
					}
				}
			}
		}

		BigDecimal healthLoadedAnnual = null;
		if (plan.getProduct().getProductType() == ProductType.HEALTH) {
			healthLoadedAnnual = computeHealthLoadedAnnual(plan, req);
		} else if (plan.getProduct().getProductType() == ProductType.LIFE) {
			LocalDate dob = req.getLifeDob();
			if (dob == null) {
				throw new BusinessRuleException("Date of birth is required for life policies");
			}
			int age = (int) ChronoUnit.YEARS.between(dob, LocalDate.now());
			if (age < 18 || age > 65) {
				throw new BusinessRuleException("Age must be between 18 and 65 years for life insurance");
			}
			if (req.getLifeSmoker() == null) {
				throw new BusinessRuleException("Please select smoker status for life policies");
			}
			// Occupation risk no longer required/used
			List<com.harshit.monocept.dto.request.PolicyPurchaseRequestNominee> nominees = req.getLifeNominees();
			if (nominees == null || nominees.isEmpty()) {
				throw new BusinessRuleException("At least one nominee is required for life policies");
			}
			int totalShare = 0;
			for (var n : nominees) {
				if (n.getName() == null || n.getName().isBlank() || n.getRelationship() == null
						|| n.getRelationship().isBlank()) {
					throw new BusinessRuleException("Nominee name and relationship are required");
				}
				Integer sp = n.getSharePct();
				if (sp == null || sp < 1 || sp > 100) {
					throw new BusinessRuleException("Nominee share must be between 1 and 100");
				}
				totalShare += sp;
			}
			if (totalShare != 100) {
				throw new BusinessRuleException("Total nominee share must be exactly 100%");
			}
		}

		Policy policy = buildPolicy(customer, plan, req.getStartDate(), req.getPremiumFrequency(),
				req.getVehicleRegistrationNumber(), req.getVehicleMake(), req.getVehicleModel(),
				req.getVehicleManufactureYear());

		if (plan.getProduct().getProductType() == ProductType.TRAVEL && req.getTripEndDate() != null) {
			policy.setEndDate(req.getTripEndDate());
		}
		// HEALTH: persist disclosures (cover type, ages, pre-existing list)
		if (plan.getProduct().getProductType() == ProductType.HEALTH) {
			policy.setHealthCoverType(req.getHealthCoverType());
			policy.setHealthInsuredAge(req.getHealthInsuredAge());
			policy.setHealthAdultCount(req.getHealthAdultCount());
			policy.setHealthChildCount(req.getHealthChildCount());
			policy.setHealthHasPreExisting(Boolean.TRUE.equals(req.getHealthHasPreExisting()));
			policy.setHealthAdultAges(req.getHealthAdultAges());
			policy.setHealthChildAges(req.getHealthChildAges());

			java.util.List<String> codes = req.getHealthPreExistingConditions();
			java.util.List<Integer> years = req.getHealthPreExistingSinceYears();
			if (codes != null && !codes.isEmpty()) {
				java.util.List<HealthPreExistingEntry> entries = new java.util.ArrayList<>();
				for (int i = 0; i < codes.size(); i++) {
					String code = codes.get(i);
					Integer since = (years != null && years.size() > i) ? years.get(i) : null;
					if (code != null) {
						entries.add(new HealthPreExistingEntry(code, since));
					}
				}
				if (!entries.isEmpty()) {
					policy.setHealthPreExisting(entries);
				}
			}
		}

		// LIFE: persist smoker/dob/nominees on policy
		if (plan.getProduct().getProductType() == ProductType.LIFE) {
			policy.setLifeDob(req.getLifeDob());
			policy.setLifeSmoker(Boolean.TRUE.equals(req.getLifeSmoker()));
			java.util.List<com.harshit.monocept.dto.request.PolicyPurchaseRequestNominee> nlist = req.getLifeNominees();
			if (nlist != null && !nlist.isEmpty()) {
				java.util.List<com.harshit.monocept.entity.LifeNomineeEntry> entries = new java.util.ArrayList<>();
				for (var n : nlist) {
					entries.add(new com.harshit.monocept.entity.LifeNomineeEntry(n.getName(), n.getRelationship(),
							n.getSharePct(), n.getDob()));
				}
				policy.setLifeNominees(entries);
			}
		}

		if (plan.getProduct().getProductType() == ProductType.HEALTH && healthLoadedAnnual != null) {
			if (plan.getPremiumType() == PremiumType.ONE_TIME) {
				policy.setInstallmentAmount(healthLoadedAnnual.setScale(2, RoundingMode.HALF_UP));
			} else {
				PremiumFrequency freq = policy.getPremiumFrequency();
				BigDecimal inst = calculateInstallmentAmount(healthLoadedAnnual, freq);
				policy.setInstallmentAmount(inst);
			}
		}

		policy = policyRepository.save(policy);

		log.info("Policy purchased: policyNumber={}, customer={}, planId={}, frequency={}", policy.getPolicyNumber(),
				email, req.getPlanId(), policy.getPremiumFrequency());

		return mapToResponse(policy);
	}

	private BigDecimal computeHealthLoadedAnnual(PolicyPlan plan, PolicyPurchaseRequest req) {
		BigDecimal base = plan.getPremiumAmount();
		if (base == null)
			base = BigDecimal.ZERO;

		// Derive age: INDIVIDUAL uses insured age; FLOATER uses max adult age
		int age = 30;
		String coverType = req.getHealthCoverType() != null ? req.getHealthCoverType() : "INDIVIDUAL";
		if ("INDIVIDUAL".equals(coverType)) {
			Integer a = req.getHealthInsuredAge();
			if (a != null && a > 0)
				age = a;
		} else {
			List<Integer> adultAges = req.getHealthAdultAges();
			if (adultAges != null && !adultAges.isEmpty()) {
				int max = 0;
				for (Integer v : adultAges) {
					if (v != null && v > max)
						max = v;
				}
				if (max > 0)
					age = max;
			}
		}

		// Age factor
		BigDecimal ageFactor = BigDecimal.ZERO; // fraction e.g., 0.07
		if (age >= 56)
			ageFactor = new BigDecimal("0.30");
		else if (age >= 46)
			ageFactor = new BigDecimal("0.15");
		else if (age >= 36)
			ageFactor = new BigDecimal("0.07");

		// Condition loadings
		java.util.Map<String, BigDecimal> LOAD = new java.util.HashMap<>();
		LOAD.put("DIABETES", new BigDecimal("0.12"));
		LOAD.put("HYPERTENSION", new BigDecimal("0.08"));
		LOAD.put("ASTHMA_COPD", new BigDecimal("0.10"));
		LOAD.put("THYROID", new BigDecimal("0.04"));
		LOAD.put("HEART_DISEASE", new BigDecimal("0.25"));
		LOAD.put("KIDNEY_LIVER", new BigDecimal("0.20"));
		LOAD.put("OTHER", new BigDecimal("0.05"));

		BigDecimal condSum = BigDecimal.ZERO;
		List<String> conds = req.getHealthPreExistingConditions();
		List<Integer> years = req.getHealthPreExistingSinceYears();
		for (int i = 0; conds != null && i < conds.size(); i++) {
			String code = conds.get(i);
			BigDecimal l = LOAD.getOrDefault(code, BigDecimal.ZERO);
			Integer since = (years != null && years.size() > i) ? years.get(i) : null;
			if (since != null) {
				int current = Year.now().getValue();
				int yrs = current - since;
				if (yrs <= 3) {
					// +50% of that condition's loading
					l = l.add(LOAD.getOrDefault(code, BigDecimal.ZERO).multiply(new BigDecimal("0.50")));
				} else if (yrs >= 10) {
					// -25% but not below 0
					BigDecimal dec = LOAD.getOrDefault(code, BigDecimal.ZERO).multiply(new BigDecimal("0.25"));
					l = l.subtract(dec);
					if (l.compareTo(BigDecimal.ZERO) < 0)
						l = BigDecimal.ZERO;
				}
			}
			condSum = condSum.add(l);
		}
		// Cap condition loadings at +60%
		if (condSum.compareTo(new BigDecimal("0.60")) > 0)
			condSum = new BigDecimal("0.60");

		// Children adjustment for floater: +2% per child capped at +8%
		BigDecimal childAdj = BigDecimal.ZERO;
		if ("FLOATER".equals(coverType)) {
			int childCount = req.getHealthChildCount() == null ? 0 : req.getHealthChildCount();
			BigDecimal add = new BigDecimal("0.02").multiply(new BigDecimal(childCount));
			if (add.compareTo(new BigDecimal("0.08")) > 0)
				add = new BigDecimal("0.08");
			if (add.compareTo(BigDecimal.ZERO) > 0)
				childAdj = add;
		}

		// Overall cap at +80%
		BigDecimal totalLoad = ageFactor.add(condSum).add(childAdj);
		if (totalLoad.compareTo(new BigDecimal("0.80")) > 0)
			totalLoad = new BigDecimal("0.80");
		if (totalLoad.compareTo(BigDecimal.ZERO) < 0)
			totalLoad = BigDecimal.ZERO;

		BigDecimal onePlus = BigDecimal.ONE.add(totalLoad);
		BigDecimal adjusted = base.multiply(onePlus);

		// Round to nearest 10
		BigDecimal ten = new BigDecimal("10");
		BigDecimal divided = adjusted.divide(ten, 0, RoundingMode.HALF_UP);
		BigDecimal rounded = divided.multiply(ten);
		return rounded.setScale(2, RoundingMode.HALF_UP);
	}

	@Transactional
	public PolicyResponse issuePolicy(PolicyIssueRequest req) {
		log.info("Policy issue attempt: customerId={}, planId={}", req.getCustomerId(), req.getPlanId());

		Customer customer = customerRepository.findById(req.getCustomerId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + req.getCustomerId()));

		PolicyPlan plan = planRepository.findById(req.getPlanId())
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + req.getPlanId()));

		if (!plan.getIsActive()) {
			log.warn("Issue attempt on inactive plan: planId={}", req.getPlanId());
			throw new BusinessRuleException("Cannot issue an inactive plan");
		}

		if (!plan.getProduct().getIsActive()) {
			log.warn("Issue attempt on inactive product: productId={}", plan.getProduct().getId());
			throw new BusinessRuleException("Cannot issue plan of an inactive product");
		}

		List<PolicyStatus> activeLikeStatuses = List.of(PolicyStatus.PENDING_PAYMENT, PolicyStatus.ACTIVE,
				PolicyStatus.LAPSED);

		if (plan.getProduct().getProductType() == ProductType.MOTOR) {

			String regNo = req.getVehicleRegistrationNumber().trim().toUpperCase().replaceAll("\\s+", "");

			if (policyRepository.existsByVehicleRegistrationNumberAndStatusIn(regNo, activeLikeStatuses)) {

				throw new BusinessRuleException("This vehicle is already insured under an active or pending policy");
			}
		}

		Policy policy = policyRepository.save(buildPolicy(customer, plan, req.getStartDate(), req.getPremiumFrequency(),
				req.getVehicleRegistrationNumber(), req.getVehicleMake(), req.getVehicleModel(),
				req.getVehicleManufactureYear()));

		// SRS LOG-007: Policy issuance log
		log.info("Policy issued: policyNumber={}, customerId={}, planId={}, frequency={}", policy.getPolicyNumber(),
				req.getCustomerId(), req.getPlanId(), policy.getPremiumFrequency());

		return mapToResponse(policy);
	}

	public Page<PolicyResponse> getMyPolicies(String email, Pageable pageable) {
		log.debug("Fetching policies for: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Customer customer = customerRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

		return policyRepository.findByCustomerId(customer.getId(), pageable).map(this::mapToResponse);
	}

	public Page<PolicyResponse> getAllPolicies(Pageable pageable) {
		log.debug("Fetching all policies, page: {}", pageable.getPageNumber());
		return policyRepository.findAll(pageable).map(this::mapToResponse);
	}

	public Page<PolicyResponse> getPoliciesByCustomer(Long customerId, Pageable pageable) {
		log.debug("Fetching policies for customerId: {}", customerId);

		customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

		return policyRepository.findByCustomerId(customerId, pageable).map(this::mapToResponse);
	}

	@Transactional
	public PolicyResponse cancelPolicy(Long policyId, String email) {
		log.info("Policy cancel attempt: policyId={}, by={}", policyId, email);

		Policy policy = policyRepository.findById(policyId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));

		if (policy.getStatus() == PolicyStatus.CANCELLED) {
			log.warn("Already cancelled policy: policyId={}", policyId);
			throw new BusinessRuleException("Policy is already cancelled");
		}

		if (policy.getStatus() == PolicyStatus.EXPIRED) {
			log.warn("Cancel attempt on expired policy: policyId={}", policyId);
			throw new BusinessRuleException("Expired policy cannot be cancelled");
		}

		policy.setStatus(PolicyStatus.CANCELLED);
		Policy saved = policyRepository.save(policy);
		log.info("Policy cancelled: policyNumber={}", saved.getPolicyNumber());

		return mapToResponse(saved);
	}

	private void validateStartDate(PolicyPlan plan, LocalDate startDate) {
		LocalDate today = LocalDate.now();

		if (plan.getProduct().getProductType() == ProductType.TRAVEL) {
			if (startDate.isAfter(today.plusDays(MAX_TRAVEL_ADVANCE_DAYS))) {
				throw new BusinessRuleException(
						"Trip start date cannot be more than " + MAX_TRAVEL_ADVANCE_DAYS + " days from today");
			}
		} else {
			if (!startDate.isEqual(today)) {
				throw new BusinessRuleException("This policy's coverage must start today (" + today
						+ "). Only travel policies can have a future start date");
			}
		}
	}

	public PolicyResponse getPolicyById(Long policyId, String email) {
		Policy policy = policyRepository.findById(policyId)
				.orElseThrow(() -> new ResourceNotFoundException("Policy not found with id: " + policyId));

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (user.getRole() == Role.CUSTOMER) {
			Customer customer = customerRepository.findByUserId(user.getId())
					.orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));
			if (!policy.getCustomer().getId().equals(customer.getId())) {
				log.warn("Customer {} attempted to access policyId={} owned by customerId={}", email, policyId,
						policy.getCustomer().getId());
				throw new BusinessRuleException("You can only access your own policies");
			}
		}

		if (policy.getStatus() == PolicyStatus.ACTIVE && policy.getEndDate().isBefore(LocalDate.now())) {
			policy.setStatus(PolicyStatus.EXPIRED);
			policyRepository.save(policy);
			log.info("Policy auto-expired: policyNumber={}", policy.getPolicyNumber());
		}

		return mapToResponse(policy);
	}

	private String generatePolicyNumber() {
		String number;
		do {
			number = "POL-" + System.currentTimeMillis() + "-"
					+ UUID.randomUUID().toString().substring(0, 4).toUpperCase();
		} while (policyRepository.existsByPolicyNumber(number));
		return number;
	}

	private Policy buildPolicy(Customer customer, PolicyPlan plan, LocalDate startDate,
			PremiumFrequency requestedFrequency, String vehicleRegNo, String vehicleMake, String vehicleModel,
			Integer vehicleYear) {

		validateStartDate(plan, startDate);

		String cleanRegNo = null;
		BigDecimal idv = null;
		Integer vehicleAge = null;
		Integer depreciationPct = null;

		if (plan.getProduct().getProductType() == ProductType.MOTOR) {
			cleanRegNo = validateVehicleDetails(vehicleRegNo, vehicleMake, vehicleModel, vehicleYear);
			vehicleAge = Year.now().getValue() - vehicleYear;
			idv = calculateIdv(plan.getCoverageAmount(), vehicleAge);
			depreciationPct = getDepreciationPercent(vehicleAge).multiply(BigDecimal.valueOf(100)).intValue();
		}

		PremiumFrequency frequency = null;
		BigDecimal installmentAmount;
		Integer totalInstallmentsDue;

		if (plan.getPremiumType() == PremiumType.ONE_TIME) {
			installmentAmount = plan.getPremiumAmount();
			totalInstallmentsDue = 1;
		} else {
			if (requestedFrequency == null) {
				throw new BusinessRuleException(
						"Please select a premium payment frequency (Monthly / Quarterly / Half-Yearly / Annual) for this plan");
			}
			frequency = requestedFrequency;
			installmentAmount = calculateInstallmentAmount(plan.getPremiumAmount(), frequency);
			totalInstallmentsDue = frequency.getInstallmentsPerYear() * plan.getDurationYears();
		}

		Policy.PolicyBuilder builder = Policy.builder().policyNumber(generatePolicyNumber()).customer(customer)
				.plan(plan).startDate(startDate).endDate(startDate.plusYears(plan.getDurationYears()))
				.status(PolicyStatus.PENDING_PAYMENT).premiumsPaid(0).premiumFrequency(frequency)
				.installmentAmount(installmentAmount).totalInstallmentsDue(totalInstallmentsDue)
				.vehicleRegistrationNumber(cleanRegNo).vehicleMake(vehicleMake != null ? vehicleMake.trim() : null)
				.vehicleModel(vehicleModel != null ? vehicleModel.trim() : null)
				.vehicleManufactureYear(plan.getProduct().getProductType() == ProductType.MOTOR ? vehicleYear : null)
				.calculatedIdv(idv).vehicleAgeAtPurchase(vehicleAge).depreciationPercentApplied(depreciationPct);

		if (plan.getPremiumType() == PremiumType.ANNUAL) {
			builder.nextPremiumDueDate(startDate);
		}

		return builder.build();
	}

	private String validateVehicleDetails(String regNo, String make, String model, Integer year) {
		if (regNo == null || regNo.isBlank()) {
			throw new BusinessRuleException("Vehicle registration number is required for motor insurance");
		}
		if (make == null || make.isBlank()) {
			throw new BusinessRuleException("Vehicle make is required for motor insurance");
		}
		if (model == null || model.isBlank()) {
			throw new BusinessRuleException("Vehicle model is required for motor insurance");
		}
		if (year == null) {
			throw new BusinessRuleException("Vehicle manufacture year is required for motor insurance");
		}

		String cleanRegNo = regNo.trim().toUpperCase().replaceAll("\\s+", "");
		if (!VEHICLE_REG_PATTERN.matcher(cleanRegNo).matches()) {
			throw new BusinessRuleException(
					"Invalid vehicle registration number format. Expected format like DL01AB1234");
		}

		int currentYear = Year.now().getValue();
		if (year > currentYear) {
			throw new BusinessRuleException("Vehicle manufacture year cannot be in the future");
		}
		if (currentYear - year > MAX_VEHICLE_AGE_YEARS) {
			throw new BusinessRuleException("This vehicle is " + (currentYear - year)
					+ " years old. We only insure vehicles up to " + MAX_VEHICLE_AGE_YEARS + " years old");
		}

		return cleanRegNo;
	}

	private BigDecimal calculateInstallmentAmount(BigDecimal annualPremium, PremiumFrequency frequency) {
		BigDecimal loadedAnnual = annualPremium.multiply(BigDecimal.ONE.add(frequency.getLoadingFactor()));
		return loadedAnnual.divide(BigDecimal.valueOf(frequency.getInstallmentsPerYear()), 2, RoundingMode.HALF_UP);
	}

	public PolicyResponse mapToResponse(Policy p) {
		Integer remainingInstallments = null;
		if (p.getPlan().getPremiumType() == PremiumType.ANNUAL) {
			int total = p.getTotalInstallmentsDue() != null ? p.getTotalInstallmentsDue() : 0;
			int paid = p.getPremiumsPaid() != null ? p.getPremiumsPaid() : 0;
			remainingInstallments = Math.max(0, total - paid);
		}

		PolicyResponse.PolicyResponseBuilder builder = PolicyResponse.builder().policyId(p.getId())
				.policyNumber(p.getPolicyNumber()).customerId(p.getCustomer().getId())
				.customerName(p.getCustomer().getUser().getFullName()).planId(p.getPlan().getId())
				.planName(p.getPlan().getPlanName()).productType(p.getPlan().getProduct().getProductType())
				.coverageAmount(p.getPlan().getCoverageAmount()).premiumAmount(p.getPlan().getPremiumAmount())
				.premiumType(p.getPlan().getPremiumType()).premiumFrequency(p.getPremiumFrequency())
				.installmentAmount(p.getInstallmentAmount()).totalInstallments(p.getTotalInstallmentsDue())
				.remainingInstallments(remainingInstallments).startDate(p.getStartDate()).endDate(p.getEndDate())
				.status(p.getStatus()).totalPremiumPaid(p.getTotalPremiumPaid()).premiumsPaid(p.getPremiumsPaid())
				.nextPremiumDueDate(p.getNextPremiumDueDate()).durationYears(p.getPlan().getDurationYears())
				.createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt())
				.vehicleRegistrationNumber(p.getVehicleRegistrationNumber()).vehicleMake(p.getVehicleMake())
				.vehicleModel(p.getVehicleModel()).vehicleManufactureYear(p.getVehicleManufactureYear())
				.calculatedIdv(p.getCalculatedIdv()).vehicleAgeAtPurchase(p.getVehicleAgeAtPurchase())
				.depreciationPercentApplied(p.getDepreciationPercentApplied()).ncbPercentage(p.getNcbPercentage());

		// Map HEALTH disclosures if present
		if (p.getPlan().getProduct().getProductType() == ProductType.HEALTH) {
			builder.healthCoverType(p.getHealthCoverType()).healthInsuredAge(p.getHealthInsuredAge())
					.healthAdultCount(p.getHealthAdultCount()).healthChildCount(p.getHealthChildCount())
					.healthHasPreExisting(p.getHealthHasPreExisting()).healthAdultAges(p.getHealthAdultAges())
					.healthChildAges(p.getHealthChildAges());

			if (p.getHealthPreExisting() != null && !p.getHealthPreExisting().isEmpty()) {
				java.util.List<HealthPreExistingDto> list = new java.util.ArrayList<>();
				for (HealthPreExistingEntry e : p.getHealthPreExisting()) {
					list.add(new HealthPreExistingDto(e.getCode(), e.getSinceYear()));
				}
				builder.healthPreExisting(list);
			}
		}

		// Map LIFE disclosures if present
		if (p.getPlan().getProduct().getProductType() == ProductType.LIFE) {
			builder.lifeDob(p.getLifeDob()).lifeSmoker(p.getLifeSmoker());
			if (p.getLifeNominees() != null && !p.getLifeNominees().isEmpty()) {
				java.util.List<com.harshit.monocept.dto.response.LifeNomineeDto> out = new java.util.ArrayList<>();
				for (com.harshit.monocept.entity.LifeNomineeEntry e : p.getLifeNominees()) {
					out.add(new com.harshit.monocept.dto.response.LifeNomineeDto(e.getName(), e.getRelationship(),
							e.getSharePct(), e.getDob()));
				}
				builder.lifeNominees(out);
			}
		}

		return builder.build();
	}
}