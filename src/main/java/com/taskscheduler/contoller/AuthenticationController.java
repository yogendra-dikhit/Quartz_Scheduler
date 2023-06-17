package com.taskscheduler.contoller;

import javax.validation.Valid;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.taskscheduler.dto.UserDTO;
import com.taskscheduler.service.UserDetailsServiceImpl;
import com.taskscheduler.util.Constants;
import com.taskscheduler.util.JwtTokenUtil;
import com.taskscheduler.wrappers.JwtResponse;
import com.taskscheduler.wrappers.ResponseWrapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/authenticate")
@RequiredArgsConstructor
public class AuthenticationController {
	private static Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

	private final AuthenticationManager authenticationManager;
	private final JwtTokenUtil jwtTokenUtil;
	private final UserDetailsServiceImpl userDetailsService;

	@PostMapping("login")
	public ResponseEntity<Object> login(@RequestBody @Valid UserDTO request) {
		try {
			Authentication authenticate = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(request.getUserName(), request.getPassword()));

			User user = (User) authenticate.getPrincipal();

			return ResponseEntity.ok().body(new JwtResponse(jwtTokenUtil.generateToken(user)));
		} catch (BadCredentialsException e) {
			logger.error("{} in AuthenticationController.login(): {}", e.getClass().getName(), e);
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
		}
	}

	@PostMapping("register")
	public ResponseEntity<Object> register(@RequestBody @Valid UserDTO user) {
		try {
			return ResponseEntity.ok(userDetailsService.save(user));
		} catch (Exception e) {
			logger.error("{} in AuthenticationController.register(): {}", e.getClass().getName(), e);
			if (e.getCause() instanceof ConstraintViolationException)
				return ResponseEntity.internalServerError()
						.body(ResponseWrapper.builder().status(Constants.Status.ERROR)
								.errorMessage(Constants.Generic.DUPLICATE_USERNAME.toString()).build());
			return ResponseEntity.internalServerError().body(ResponseWrapper.builder()
					.status(Constants.Status.ERROR).errorMessage(e.getMessage()).build());
		}
	}
}
