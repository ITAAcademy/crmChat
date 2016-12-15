package com.intita.wschat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value=HttpStatus.METHOD_NOT_ALLOWED, reason="ChatUser not in room")  // 404
public class ChatUserNotInRoomException extends RuntimeException{
	private static final long serialVersionUID = 1L;

	public ChatUserNotInRoomException(String message) {
		super(message);
	}
}
