package com.eleks.groupservice.handler;

import com.eleks.groupservice.dto.ErrorDto;
import com.eleks.groupservice.exception.ResourceNotFoundException;
import com.eleks.groupservice.exception.UserServiceException;
import com.eleks.groupservice.exception.UsersIdsValidationException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Collections.singletonList;
import static org.springframework.http.HttpStatus.*;

@ControllerAdvice
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(UserServiceException.class)
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorDto handleUserServiceException(UserServiceException exception) {
        return createError(INTERNAL_SERVER_ERROR, singletonList(exception.getMessage()));
    }

    @ExceptionHandler(UsersIdsValidationException.class)
    @ResponseStatus(BAD_REQUEST)
    @ResponseBody
    public ErrorDto handleInvalidGroupMembersIdsException(UsersIdsValidationException exception) {
        return createError(BAD_REQUEST, singletonList(exception.getMessage()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(NOT_FOUND)
    @ResponseBody
    public ErrorDto handleNotFoundException(ResourceNotFoundException exception) {
        return createError(NOT_FOUND, singletonList(exception.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        ErrorDto error = createError(status, messages);
        return new ResponseEntity<>(error, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        String msg;
        if (ex.getCause() instanceof InvalidFormatException) {
            msg = ex.getCause().getMessage();
        } else {
            msg = ex.getMessage();
        }
        ErrorDto error = createError(status, singletonList(msg));
        return new ResponseEntity<>(error, headers, status);
    }

    private ErrorDto createError(HttpStatus status, List<String> messages) {
        return ErrorDto.builder()
                .statusCode(status.value())
                .messages(messages)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

