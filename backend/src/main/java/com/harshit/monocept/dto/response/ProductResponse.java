package com.harshit.monocept.dto.response;

import java.time.LocalDateTime;

import com.harshit.monocept.enums.ProductType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
	private Long productId;
	private String productName;
	private ProductType productType;
	private String description;
	private Boolean isActive;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}