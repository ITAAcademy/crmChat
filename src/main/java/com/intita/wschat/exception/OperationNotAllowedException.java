package com.intita.wschat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.METHOD_NOT_ALLOWED, reason="Operation not allowed")
public class OperationNotAllowedException extends Exception {
 public OperationNotAllowedException() {

 }
 public OperationNotAllowedException(String body) {
     super(body);
 }
}
