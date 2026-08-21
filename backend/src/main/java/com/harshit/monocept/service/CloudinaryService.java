package com.harshit.monocept.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.harshit.monocept.exception.BusinessRuleException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CloudinaryService {

	private static final Logger log = LoggerFactory.getLogger(CloudinaryService.class);

	private final Cloudinary cloudinary;

	private static final List<String> ALLOWED_TYPES = Arrays.asList("pdf", "jpg", "jpeg", "png", "doc", "docx");

	private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024; // 10MB

	public Map<String, String> uploadFile(MultipartFile file, String claimNumber) {
		log.info("Upload attempt: file={}, size={}, claim={}", file.getOriginalFilename(), file.getSize(), claimNumber);

		// Validation 1: Empty file
		if (file == null || file.isEmpty())
			throw new BusinessRuleException("File cannot be empty");

		// Validation 2: File size
		if (file.getSize() > MAX_SIZE_BYTES)
			throw new BusinessRuleException(
					"File size " + (file.getSize() / 1024 / 1024) + "MB exceeds maximum 10MB limit");

		// Validation 3: File extension
		String originalName = file.getOriginalFilename();
		if (originalName == null || !originalName.contains("."))
			throw new BusinessRuleException("Invalid file — no extension found");

		String ext = originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase();

		if (!ALLOWED_TYPES.contains(ext))
			throw new BusinessRuleException(
					"File type '." + ext + "' not allowed. " + "Allowed: pdf, jpg, jpeg, png, doc, docx");

		try {
			String publicId = "insurance-claims/" + claimNumber + "/" + UUID.randomUUID().toString().substring(0, 8);

			Map params = ObjectUtils.asMap("public_id", publicId, "resource_type", "auto", "overwrite", false);

			Map uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

			String url = (String) uploadResult.get("secure_url");
			String pubId = (String) uploadResult.get("public_id");
			String format = (String) uploadResult.get("format");
			Object bytesObj = uploadResult.get("bytes");
			long size = bytesObj instanceof Number ? ((Number) bytesObj).longValue() : file.getSize();

			log.info("Upload success: publicId={}", pubId);

			return Map.of("url", url, "publicId", pubId, "format", format != null ? format : ext, "size",
					String.valueOf(size), "originalName", originalName);

		} catch (IOException e) {
			log.error("Upload failed", e);
			e.printStackTrace();
			throw new BusinessRuleException("Upload failed: " + e.getMessage());
		}
	}

	public void deleteFile(String publicId) {
		if (publicId == null || publicId.isBlank()) {
			log.warn("Cloudinary delete skipped: publicId is blank");
			return;
		}

		log.info("Cloudinary delete attempt: publicId={}", publicId);

		try {
			Map imageResult = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image"));

			Object imageStatus = imageResult.get("result");
			log.info("Cloudinary image delete result for {}: {}", publicId, imageStatus);

			if ("ok".equalsIgnoreCase(String.valueOf(imageStatus))) {
				return;
			}

			Map rawResult = cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw"));

			Object rawStatus = rawResult.get("result");
			log.info("Cloudinary raw delete result for {}: {}", publicId, rawStatus);

			if (!"ok".equalsIgnoreCase(String.valueOf(rawStatus))
					&& !"not found".equalsIgnoreCase(String.valueOf(rawStatus))) {
				throw new RuntimeException("Cloudinary delete failed with result: " + rawStatus);
			}

		} catch (Exception e) {
			log.error("Cloudinary delete failed for publicId={}", publicId, e);
			throw new RuntimeException("Could not delete file from Cloudinary.", e);
		}
	}
}