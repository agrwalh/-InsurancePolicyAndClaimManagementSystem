package com.harshit.monocept.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.harshit.monocept.dto.request.PlanRequest;
import com.harshit.monocept.dto.response.PlanResponse;
import com.harshit.monocept.entity.InsuranceProduct;
import com.harshit.monocept.entity.PolicyPlan;
import com.harshit.monocept.exception.BusinessRuleException;
import com.harshit.monocept.exception.ResourceNotFoundException;
import com.harshit.monocept.repository.PolicyPlanRepository;
import com.harshit.monocept.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PolicyPlanService {

	private static final Logger log = LoggerFactory.getLogger(PolicyPlanService.class);

	private final PolicyPlanRepository planRepository;
	private final ProductRepository productRepository;

	public PlanResponse createPlan(PlanRequest req) {
		log.info("Plan creation attempt: name={}, productId={}", req.getPlanName(), req.getProductId());

		InsuranceProduct product = productRepository.findById(req.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + req.getProductId()));

		if (!product.getIsActive()) {
			log.warn("Plan creation on inactive product: productId={}", req.getProductId());
			throw new BusinessRuleException("Cannot create plan for inactive product");
		}

		if (planRepository.existsByProductIdAndPlanNameIgnoreCase(req.getProductId(), req.getPlanName().trim())) {
			throw new BusinessRuleException("A plan with this name already exists under the selected product");
		}

		if (req.getCoverageAmount().compareTo(req.getPremiumAmount()) <= 0) {
			log.warn("Coverage <= Premium: coverage={}, premium={}", req.getCoverageAmount(), req.getPremiumAmount());
			throw new BusinessRuleException("Coverage amount must be greater than premium amount");
		}

		PolicyPlan plan = PolicyPlan.builder().product(product).planName(req.getPlanName())
				.coverageAmount(req.getCoverageAmount()).premiumAmount(req.getPremiumAmount())
				.premiumType(req.getPremiumType()).durationYears(req.getDurationYears())
				.termsAndConditions(req.getTermsAndConditions())
				.isActive(req.getIsActive() != null ? req.getIsActive() : true).build();

		PolicyPlan saved = planRepository.save(plan);
		log.info("Plan created: id={}, name={}, productId={}", saved.getId(), saved.getPlanName(), product.getId());

		return mapToResponse(saved);
	}

	public PlanResponse updatePlan(Long id, PlanRequest req) {
		log.info("Plan update attempt: id={}", id);

		PolicyPlan plan = planRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

		InsuranceProduct product = productRepository.findById(req.getProductId())
				.orElseThrow(() -> new ResourceNotFoundException("Product not found"));

		if (!product.getIsActive()) {
			log.warn("Plan update on inactive product: productId={}", req.getProductId());
			throw new BusinessRuleException("Cannot link plan to inactive product");
		}

		if (planRepository.existsByProductIdAndPlanNameIgnoreCaseAndIdNot(req.getProductId(), req.getPlanName().trim(),
				id)) {
			throw new BusinessRuleException("A plan with this name already exists under the selected product");
		}

		if (req.getCoverageAmount().compareTo(req.getPremiumAmount()) <= 0) {
			log.warn("Coverage <= Premium on update: coverage={}, premium={}", req.getCoverageAmount(),
					req.getPremiumAmount());
			throw new BusinessRuleException("Coverage amount must be greater than premium amount");
		}

		plan.setProduct(product);
		plan.setPlanName(req.getPlanName());
		plan.setCoverageAmount(req.getCoverageAmount());
		plan.setPremiumAmount(req.getPremiumAmount());
		plan.setPremiumType(req.getPremiumType());
		plan.setDurationYears(req.getDurationYears());
		plan.setTermsAndConditions(req.getTermsAndConditions());
		if (req.getIsActive() != null)
			plan.setIsActive(req.getIsActive());

		PolicyPlan updated = planRepository.save(plan);
		log.info("Plan updated: id={}, name={}", updated.getId(), updated.getPlanName());

		return mapToResponse(updated);
	}

	public PlanResponse deactivatePlan(Long id) {
		log.info("Plan deactivation attempt: id={}", id);

		PolicyPlan plan = planRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Plan not found with id: " + id));

		if (!plan.getIsActive()) {
			log.warn("Plan already inactive: id={}", id);
			throw new BusinessRuleException("Plan is already inactive");
		}

		plan.setIsActive(false);
		PolicyPlan saved = planRepository.save(plan);
		log.info("Plan deactivated: id={}, name={}", saved.getId(), saved.getPlanName());

		return mapToResponse(saved);
	}

	public Page<PlanResponse> getActivePlans(Pageable pageable) {
		log.debug("Fetching active plans, page: {}", pageable.getPageNumber());
		return planRepository.findByIsActiveTrue(pageable).map(this::mapToResponse);
	}

	public Page<PlanResponse> getPlansByProduct(Long productId, Pageable pageable) {
		log.debug("Fetching plans for productId: {}", productId);

		productRepository.findById(productId)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

		return planRepository.findByProductIdAndIsActiveTrue(productId, pageable).map(this::mapToResponse);
	}

	private PlanResponse mapToResponse(PolicyPlan p) {
		return PlanResponse.builder().planId(p.getId()).productId(p.getProduct().getId())
				.productName(p.getProduct().getProductName()).productType(p.getProduct().getProductType())
				.planName(p.getPlanName()).coverageAmount(p.getCoverageAmount()).premiumAmount(p.getPremiumAmount())
				.premiumType(p.getPremiumType()).durationYears(p.getDurationYears())
				.termsAndConditions(p.getTermsAndConditions()).isActive(p.getIsActive()).createdAt(p.getCreatedAt())
				.updatedAt(p.getUpdatedAt()).build();
	}
}