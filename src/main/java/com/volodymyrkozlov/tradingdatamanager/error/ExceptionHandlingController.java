package com.volodymyrkozlov.tradingdatamanager.error;

import com.volodymyrkozlov.tradingdatamanager.repository.EntityNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
public class ExceptionHandlingController {

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseError> handle(IllegalStateException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseError> handle(IllegalArgumentException ex) {
        return ResponseEntity.status(BAD_REQUEST)
                .body(new ResponseError(ex.getMessage()));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseError> handle(EntityNotFoundException ex) {
        return ResponseEntity.status(NOT_FOUND)
                .body(new ResponseError(ex.getMessage()));
    }
}
