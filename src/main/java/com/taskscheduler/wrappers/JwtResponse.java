package com.taskscheduler.wrappers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class JwtResponse {

	private final String jwtToken;

}
