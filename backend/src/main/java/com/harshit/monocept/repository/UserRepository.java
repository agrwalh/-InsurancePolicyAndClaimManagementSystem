package com.harshit.monocept.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.harshit.monocept.entity.User;
import com.harshit.monocept.enums.Role;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	Page<User> findByRole(Role role, Pageable pageable);

	Page<User> findByIsActive(Boolean isActive, Pageable pageable);
}