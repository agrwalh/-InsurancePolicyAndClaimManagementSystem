package com.harshit.monocept.exception;

import java.util.HashMap;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.harshit.monocept.dto.response.ErrorResponse;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), request);
	}

	@ExceptionHandler(DuplicateResourceException.class)
	public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
		return buildError(HttpStatus.CONFLICT, "DUPLICATE_RESOURCE", ex.getMessage(), request);
	}

	@ExceptionHandler(BusinessRuleException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessRuleException ex, HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "BUSINESS_RULE_VIOLATION", ex.getMessage(), request);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
			HttpServletRequest request) {
		Map<String, String> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(e -> {
			String field = ((FieldError) e).getField();
			errors.put(field, e.getDefaultMessage());
		});
		return buildError(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Validation failed: " + errors, request);
	}

	@ExceptionHandler(BadCredentialsException.class)
	public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
		return buildError(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "Invalid email or password", request);
	}

	@ExceptionHandler(AccessDeniedException.class)
	public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
		return buildError(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied - insufficient permissions", request);
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
			HttpServletRequest request) {
		return buildError(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", ex.getMessage(), request);
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
		log.error("Unexpected system error", ex);
		return buildError(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "Something went wrong", request);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleMaxSize(MaxUploadSizeExceededException ex, HttpServletRequest request) {
		log.warn("File size exceeded: {}", ex.getMessage());
		return buildError(HttpStatus.BAD_REQUEST, "FILE_SIZE_EXCEEDED",
				"File size exceeds maximum allowed limit of 10MB", request);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
			HttpServletRequest request) {
		log.warn("Unsupported HTTP method: {} {}", request.getMethod(), request.getRequestURI());
		return buildError(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED",
				"This HTTP method is not supported for this endpoint", request);
	}

	@ExceptionHandler(NoHandlerFoundException.class)
	public ResponseEntity<ErrorResponse> handleNoHandlerFound(NoHandlerFoundException ex, HttpServletRequest request) {
		return buildError(HttpStatus.NOT_FOUND, "NOT_FOUND", "No endpoint found for this URL", request);
	}

	private ResponseEntity<ErrorResponse> buildError(HttpStatus status, String errorType, String message,
			HttpServletRequest request) {
		return ResponseEntity.status(status).body(ErrorResponse.builder().statusCode(status.value())
				.errorType(errorType).message(message).path(request.getRequestURI()).build());
	}
}