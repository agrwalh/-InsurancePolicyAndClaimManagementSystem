package com.harshit.monocept.dto.response;

import java.time.LocalDateTime;

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
public class ErrorResponse {

	@Builder.Default
	private LocalDateTime timestamp = LocalDateTime.now();

	private int statusCode;
	private String errorType;
	private String message;
	private String path;
}