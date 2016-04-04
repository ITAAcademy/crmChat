package com.intita.wschat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No such room")  // 404
public class RoomNotFoundException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public RoomNotFoundException(String message) {
		super(message);
	}
}
