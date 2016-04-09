package com.intita.wschat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.NOT_FOUND, reason="No such user")  // 404
public class ChatUserNotFoundException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ChatUserNotFoundException(String message) {
		super(message);
	}
}