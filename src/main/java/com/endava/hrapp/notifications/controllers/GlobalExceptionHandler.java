package com.endava.hrapp.notifications.controllers;

import com.endava.hrapp.notifications.services.ServiceException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.MethodNotAllowedException;

import java.io.IOException;

@RestControllerAdvice
public class GlobalExceptionHandler  {

    @ExceptionHandler({NumberFormatException.class,HttpMessageNotReadableException.class,
            NullPointerException.class, IOException.class, IllegalArgumentException.class,
            ConversionFailedException.class
            })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity badInputRequest() throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        return ResponseEntity.status(400)
                .body(mapper.readTree("{\"Message\":\"Bad Input Parameter. The URL or JSON entered" +
                        " are wrong.\"}"));
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResponseEntity<JsonNode> methodNotAllowed(MethodNotAllowedException ex) throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        return ResponseEntity.status(405)
                .body(mapper.readTree("{\"Message\":\"Method"+ex.getHttpMethod()+"not allowed\"}"));
    }

    @ExceptionHandler(ServiceException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<JsonNode> internalServeError(ServiceException e) throws IOException {
        ObjectMapper mapper=new ObjectMapper();
        Logger logger=LoggerFactory.getLogger(GlobalExceptionHandler.class);
        logger.error("Message: " + e.getMessage()+ "\t Cause:  " +  e.getCause());
        return ResponseEntity.status(500)
                .body(mapper.readTree("{\"Message\":\"Internal Server Error\"}"));
    }
}
