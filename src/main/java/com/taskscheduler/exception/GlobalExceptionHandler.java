package com.taskscheduler.exception;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.taskscheduler.util.Constants;

@ResponseBody
@ControllerAdvice
public class GlobalExceptionHandler {

	private Logger logger = LogManager.getLogger(getClass());

	@ResponseStatus(HttpStatus.BAD_REQUEST)
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorDetails> handleValidationExceptions(MethodArgumentNotValidException ex) {
		Map<String, Object> errors = new HashMap<>();
		ex.getBindingResult().getAllErrors().forEach(error -> {
			String fieldName = ((FieldError) error).getField();
			String errorMessage = error.getDefaultMessage();
			errors.put(fieldName, errorMessage);
		});
		logger.error("Validation Errors: {}", errors);
		ErrorDetails errorDetails = new ErrorDetails(LocalDateTime.now(),
				String.valueOf(HttpStatus.BAD_REQUEST.value()), Constants.Generic.VALIDATION_FAILED.toString(), errors);
		return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
	}

}
