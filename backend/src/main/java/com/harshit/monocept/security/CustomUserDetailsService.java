package com.harshit.monocept.security;

import java.util.List;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.harshit.monocept.entity.User;
import com.harshit.monocept.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new UsernameNotFoundException("User not found"));

		if (!user.getIsActive()) {
			throw new UsernameNotFoundException("Account is inactive");
		}

		return org.springframework.security.core.userdetails.User.builder().username(user.getEmail())
				.password(user.getPassword())
				.authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))).build();
	}
}