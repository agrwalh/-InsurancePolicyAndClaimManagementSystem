package com.harshit.monocept.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.harshit.monocept.dto.request.ProductRequest;
import com.harshit.monocept.dto.response.ProductResponse;
import com.harshit.monocept.entity.InsuranceProduct;
import com.harshit.monocept.exception.BusinessRuleException;
import com.harshit.monocept.exception.DuplicateResourceException;
import com.harshit.monocept.exception.ResourceNotFoundException;
import com.harshit.monocept.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

	private static final Logger log = LoggerFactory.getLogger(ProductService.class);

	private final ProductRepository productRepository;

	public ProductResponse createProduct(ProductRequest req) {
		log.info("Product creation attempt: name={}, type={}", req.getProductName(), req.getProductType());

		if (productRepository.existsByProductName(req.getProductName())) {
			log.warn("Duplicate product name: {}", req.getProductName());
			throw new DuplicateResourceException("Product already exists: " + req.getProductName());
		}

		InsuranceProduct product = InsuranceProduct.builder().productName(req.getProductName())
				.productType(req.getProductType()).description(req.getDescription())
				.isActive(req.getIsActive() != null ? req.getIsActive() : true).build();

		InsuranceProduct saved = productRepository.save(product);
		log.info("Product created: id={}, name={}", saved.getId(), saved.getProductName());

		return mapToResponse(saved);
	}

	public ProductResponse updateProduct(Long id, ProductRequest req) {
		log.info("Product update attempt: id={}", id);

		InsuranceProduct product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

		if (!product.getProductName().equals(req.getProductName())
				&& productRepository.existsByProductName(req.getProductName())) {
			log.warn("Duplicate product name on update: {}", req.getProductName());
			throw new DuplicateResourceException("Product name already exists: " + req.getProductName());
		}

		product.setProductName(req.getProductName());
		product.setProductType(req.getProductType());
		product.setDescription(req.getDescription());
		if (req.getIsActive() != null)
			product.setIsActive(req.getIsActive());

		InsuranceProduct updated = productRepository.save(product);
		log.info("Product updated: id={}, name={}", updated.getId(), updated.getProductName());

		return mapToResponse(updated);
	}

	public ProductResponse deactivateProduct(Long id) {
		log.info("Product deactivation attempt: id={}", id);

		InsuranceProduct product = productRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

		if (!product.getIsActive()) {
			log.warn("Product already inactive: id={}", id);
			throw new BusinessRuleException("Product is already inactive");
		}

		product.setIsActive(false);
		InsuranceProduct saved = productRepository.save(product);
		log.info("Product deactivated: id={}, name={}", saved.getId(), saved.getProductName());

		return mapToResponse(saved);
	}

	public Page<ProductResponse> getActiveProducts(Pageable pageable) {
		log.debug("Fetching active products, page: {}", pageable.getPageNumber());
		return productRepository.findByIsActiveTrue(pageable).map(this::mapToResponse);
	}

	public Page<ProductResponse> getAllProducts(Pageable pageable) {
		log.debug("Fetching all products, page: {}", pageable.getPageNumber());
		return productRepository.findAll(pageable).map(this::mapToResponse);
	}

	private ProductResponse mapToResponse(InsuranceProduct p) {
		return ProductResponse.builder().productId(p.getId()).productName(p.getProductName())
				.productType(p.getProductType()).description(p.getDescription()).isActive(p.getIsActive())
				.createdAt(p.getCreatedAt()).updatedAt(p.getUpdatedAt()).build();
	}
}