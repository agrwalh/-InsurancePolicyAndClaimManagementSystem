package com.harshit.monocept.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.harshit.monocept.dto.response.ApiResponse;
import com.harshit.monocept.dto.response.DocumentUploadResponse;
import com.harshit.monocept.service.ClaimDocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/claims")
@RequiredArgsConstructor
public class ClaimDocumentController {

	private final ClaimDocumentService documentService;

	@PostMapping(value = "/{claimId}/documents", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<DocumentUploadResponse>> upload(@PathVariable Long claimId,
			@RequestParam("documentName") String documentName, @RequestParam("documentType") String documentType,
			@RequestParam("file") MultipartFile file, Authentication auth) {

		DocumentUploadResponse response = documentService.uploadDocument(claimId, documentName, documentType, file,
				auth.getName());

		return ResponseEntity.status(HttpStatus.CREATED)
				.body(ApiResponse.success("Document uploaded successfully", response));
	}

	@GetMapping("/{claimId}/documents")
	@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<List<DocumentUploadResponse>>> getDocuments(@PathVariable Long claimId,
			Authentication auth) {

		return ResponseEntity.ok(
				ApiResponse.success("Documents fetched", documentService.getClaimDocuments(claimId, auth.getName())));
	}

	@DeleteMapping("/documents/{documentId}")
	@PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN') or hasRole('AGENT')")
	public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long documentId, Authentication auth) {

		documentService.deleteDocument(documentId, auth.getName());
		return ResponseEntity
				.ok(ApiResponse.success("Document deleted successfully", "Document ID " + documentId + " deleted"));
	}
}