package com.harshit.monocept.controller;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.harshit.monocept.dto.request.ProductRequest;
import com.harshit.monocept.dto.response.ApiResponse;
import com.harshit.monocept.dto.response.PagedResponse;
import com.harshit.monocept.dto.response.ProductResponse;
import com.harshit.monocept.service.ProductService;
import com.harshit.monocept.util.PaginationUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @RequestBody ProductRequest req) {
		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Product created", productService.createProduct(req)));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable Long id,
			@Valid @RequestBody ProductRequest req) {
		return ResponseEntity.ok(ApiResponse.success("Product updated", productService.updateProduct(id, req)));
	}

	@PatchMapping("/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<ProductResponse>> deactivate(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Product deactivated", productService.deactivateProduct(id)));
	}

	@GetMapping("/active")
	public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getActive(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {

		Page<ProductResponse> result = productService.getActiveProducts(
				PaginationUtil.createPageable(page, size, sortBy, direction, PaginationUtil.PRODUCT_SORT_FIELDS));
		return ResponseEntity.ok(ApiResponse.success("Active products", PagedResponse.from(result, sortBy, direction)));
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ApiResponse<PagedResponse<ProductResponse>>> getAll(
			@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
			@RequestParam(defaultValue = "createdAt") String sortBy,
			@RequestParam(defaultValue = "desc") String direction) {

		Page<ProductResponse> result = productService.getAllProducts(
				PaginationUtil.createPageable(page, size, sortBy, direction, PaginationUtil.PRODUCT_SORT_FIELDS));
		return ResponseEntity.ok(ApiResponse.success("All products", PagedResponse.from(result, sortBy, direction)));
	}
}