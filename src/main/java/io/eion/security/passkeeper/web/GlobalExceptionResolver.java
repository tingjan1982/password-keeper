package io.eion.security.passkeeper.web;

import io.eion.security.passkeeper.service.exception.SecureAccountException;
import io.eion.security.passkeeper.service.exception.SecureAccountNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Created by vagrant on 9/19/16.
 */
@ControllerAdvice
public class GlobalExceptionResolver {

    @ExceptionHandler(SecureAccountException.class)
    public ResponseEntity secureAccountException(final SecureAccountException secureAccountException) {

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(secureAccountException.getMessage());
    }

    @ExceptionHandler(SecureAccountNotFoundException.class)
    public ResponseEntity secureAccountNotFoundException(final SecureAccountNotFoundException exception) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exception.getMessage());
    }

}
