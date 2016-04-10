package com.intita.wschat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No such course")  // 404
public class CourseNotFoundException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public CourseNotFoundException(String message) {
		super(message);
	}
}