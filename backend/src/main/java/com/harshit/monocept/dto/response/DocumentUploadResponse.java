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
public class DocumentUploadResponse {
	private Long documentId;
	private Long claimId;
	private String claimNumber;
	private String documentName;
	private String documentType;
	private String documentUrl; 
	private String originalFileName;
	private String fileFormat;
	private Long fileSizeBytes;
	private String fileSizeReadable; 
	private LocalDateTime uploadedAt;
}