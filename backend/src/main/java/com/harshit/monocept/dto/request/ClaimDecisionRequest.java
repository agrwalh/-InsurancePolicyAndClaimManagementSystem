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
public class ClaimDecisionRequest {

	@NotNull(message = "Decision is required")
	private ClaimStatus finalStatus;

	@NotBlank(message = "Remarks are required")
	private String remarks;
}