package com.harshit.monocept.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.harshit.monocept.dto.request.CustomerRequest;
import com.harshit.monocept.dto.response.CustomerResponse;
import com.harshit.monocept.entity.Customer;
import com.harshit.monocept.entity.User;
import com.harshit.monocept.exception.BusinessRuleException;
import com.harshit.monocept.exception.ResourceNotFoundException;
import com.harshit.monocept.repository.CustomerRepository;
import com.harshit.monocept.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerService {

	private static final Logger log = LoggerFactory.getLogger(CustomerService.class);

	private final CustomerRepository customerRepository;
	private final UserRepository userRepository;
	
	@Transactional
	public CustomerResponse createProfile(CustomerRequest req, String email) {
		log.info("Profile creation attempt for user: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		if (customerRepository.existsByUserId(user.getId())) {
			log.warn("Profile already exists for user: {}", email);
			throw new BusinessRuleException("Profile already exists for this user");
		}

		Customer customer = Customer.builder().user(user).dateOfBirth(req.getDateOfBirth()).address(req.getAddress())
				.city(req.getCity()).state(req.getState()).pinCode(req.getPinCode()).nomineeName(req.getNomineeName())
				.nomineeRelation(req.getNomineeRelation()).build();

		Customer saved = customerRepository.save(customer);
		log.info("Customer profile created: customerId={}, userId={}", saved.getId(), user.getId());

		return mapToResponse(saved);
	}

	public CustomerResponse updateProfile(CustomerRequest req, String email) {
		log.info("Profile update attempt for user: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Customer customer = customerRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Profile not found. Create profile first."));

		customer.setDateOfBirth(req.getDateOfBirth());
		customer.setAddress(req.getAddress());
		customer.setCity(req.getCity());
		customer.setState(req.getState());
		customer.setPinCode(req.getPinCode());
		customer.setNomineeName(req.getNomineeName());
		customer.setNomineeRelation(req.getNomineeRelation());

		Customer updated = customerRepository.save(customer);
		log.info("Customer profile updated: customerId={}", updated.getId());

		return mapToResponse(updated);
	}

	public CustomerResponse getMyProfile(String email) {
		log.debug("Fetching profile for user: {}", email);

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new ResourceNotFoundException("User not found"));

		Customer customer = customerRepository.findByUserId(user.getId())
				.orElseThrow(() -> new ResourceNotFoundException("Profile not found"));

		return mapToResponse(customer);
	}

	public CustomerResponse getCustomerById(Long customerId) {
		log.debug("Fetching customer by id: {}", customerId);

		Customer customer = customerRepository.findById(customerId)
				.orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));
		return mapToResponse(customer);
	}

	public Page<CustomerResponse> getAllCustomers(Pageable pageable) {
		log.debug("Fetching all customers, page: {}", pageable.getPageNumber());
		return customerRepository.findAll(pageable).map(this::mapToResponse);
	}

	private CustomerResponse mapToResponse(Customer c) {
		return CustomerResponse.builder().customerId(c.getId()).fullName(c.getUser().getFullName())
				.email(c.getUser().getEmail()).mobileNumber(c.getUser().getMobileNumber())
				.dateOfBirth(c.getDateOfBirth()).address(c.getAddress()).city(c.getCity()).state(c.getState())
				.pinCode(c.getPinCode()).nomineeName(c.getNomineeName()).nomineeRelation(c.getNomineeRelation())
				.createdAt(c.getCreatedAt()).updatedAt(c.getUpdatedAt()).build();
	}
}