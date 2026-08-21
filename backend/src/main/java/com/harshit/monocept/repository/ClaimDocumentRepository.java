package com.harshit.monocept.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harshit.monocept.entity.ClaimDocument;

@Repository
public interface ClaimDocumentRepository extends JpaRepository<ClaimDocument, Long> {
	List<ClaimDocument> findByClaimId(Long claimId);
}