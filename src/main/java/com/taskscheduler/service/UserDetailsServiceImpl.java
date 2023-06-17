package com.taskscheduler.service;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.taskscheduler.dto.UserDTO;
import com.taskscheduler.model.UserModel;
import com.taskscheduler.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserModel user = userRepository.findByUserName(username);
		if (user == null)
			throw new UsernameNotFoundException("User not found with username: " + username);

		return new User(user.getUserName(), user.getPassword(), new ArrayList<>());
	}

	public boolean existsByUserName(String userName) {
		return userRepository.findByUserName(userName) == null;
	}

	public UserModel save(UserDTO user) {
		UserModel newUser = new UserModel();
		newUser.setUserName(user.getUserName());
		newUser.setPassword(passwordEncoder.encode(user.getPassword()));
		return userRepository.save(newUser);
	}

}
