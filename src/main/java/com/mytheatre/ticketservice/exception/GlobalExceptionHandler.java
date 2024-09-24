package com.mytheatre.ticketservice.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;


@ControllerAdvice
public class GlobalExceptionHandler {

    private static Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleException(Exception e) {
        logger.error("Error while processing the request {1}", e.getMessage());
        return new ResponseEntity<>("An error occurred while processing the request -: "+ e.getMessage(), HttpStatus.BAD_REQUEST);
    }
}
