package com.intita.wschat.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.orm.jpa.JpaObjectRetrievalFailureException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

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
}
