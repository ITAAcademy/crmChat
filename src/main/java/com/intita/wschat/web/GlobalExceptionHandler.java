package com.intita.wschat.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.intita.wschat.exception.TooMuchProfanityException;

import javax.naming.OperationNotSupportedException;

@ControllerAdvice
public class GlobalExceptionHandler {
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	@ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler(NumberFormatException.class)
    public void handleConflict(NumberFormatException e) {
        log.info(e.getMessage());
    }
	@ExceptionHandler(JpaObjectRetrievalFailureException.class)
	public void handleConflict(ObjectRetrievalFailureException  e) {
		log.info(e.getMessage());
	}
	
	@MessageExceptionHandler(MessageDeliveryException.class)
	public String handleMessageDeliveryException(MessageDeliveryException e) {
		//log.error("MessageDeliveryException handler executed");
		return "MessageDeliveryException handler executed";
	}
	@MessageExceptionHandler(NumberFormatException.class)
	public String handleNumberFormatException(Exception ex) {
		//logger.error("NumberFormatException handler executed");
		return "NumberFormatException handler executed";
	}
	@MessageExceptionHandler(Exception.class)
	public String handleMessageException(Exception ex) {
		//log.error("NumberFormatException handler executed");
		return "NumberFormatException handler executed";
	}
	@MessageExceptionHandler
	@SendToUser(value = "/exchange/amq.direct/errors", broadcast = false)
	public String handleProfanity(TooMuchProfanityException e) {
		return e.getMessage();
	}



}
