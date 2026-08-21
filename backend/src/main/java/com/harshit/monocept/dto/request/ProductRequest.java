package com.harshit.monocept.dto.request;

import com.harshit.monocept.enums.ProductType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequest {

	@NotBlank(message = "Product name is required")
	private String productName;

	@NotNull(message = "Product type is required")
	private ProductType productType;

	@NotBlank(message = "Description is required")
	@Size(min = 10, message = "Description must be at least 10 characters")
	private String description;

	private Boolean isActive = true;
}