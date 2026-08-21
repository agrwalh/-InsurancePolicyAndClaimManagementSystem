package com.harshit.monocept.dto.request;

import com.harshit.monocept.enums.ClaimStatus;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClaimReviewRequest {

	@NotNull(message = "Status is required")
	private ClaimStatus recommendedStatus;

	@NotBlank(message = "Remarks are required")
	private String remarks;
}