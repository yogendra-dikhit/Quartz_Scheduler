package com.taskscheduler.exception;

import java.time.LocalDateTime;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonInclude(value = Include.NON_NULL)
public class ErrorDetails {

	private LocalDateTime timestamp;
	private String status;
	private String description;
	private Map<String, Object> errors;

	public ErrorDetails(LocalDateTime timestamp, String status, String description) {
		super();
		this.timestamp = timestamp;
		this.status = status;
		this.description = description;
	}

	public ErrorDetails(LocalDateTime timestamp, String status, String description, Map<String, Object> errors) {
		super();
		this.timestamp = timestamp;
		this.status = status;
		this.description = description;
		this.errors = errors;
	}

}
