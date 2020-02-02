package com.eleks.userservice.handler;

import com.eleks.common.dto.ErrorDto;
import com.eleks.userservice.exception.InvalidDateFormatException;
import com.eleks.userservice.exception.ResourceNotFoundException;
import com.eleks.userservice.exception.UniqueUserPropertiesViolationException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authentication.BadCredentialsException;
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
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public com.eleks.common.dto.ErrorDto handleNotFoundException(Exception exception) {
        log.info("handleNotFoundException, " + exception.getMessage());
        String msg = exception.getMessage() == null ? "Resource not found" : exception.getMessage();
        return createError(HttpStatus.NOT_FOUND, Collections.singletonList(msg));
    }

    @ExceptionHandler(UniqueUserPropertiesViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public com.eleks.common.dto.ErrorDto handleBadUserDataException(Exception exception) {
        log.info("handleBadUserDataException, " + exception.getMessage());
        String msg = exception.getMessage() == null ? "User data is incorrect" : exception.getMessage();
        return createError(HttpStatus.BAD_REQUEST, Collections.singletonList(msg));
    }

    @ExceptionHandler(BadCredentialsException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ResponseBody
    public com.eleks.common.dto.ErrorDto handleBadCredentialsException(BadCredentialsException exception) {
        log.info("handleBadCredentialsException, " + exception.getMessage());
        return createError(HttpStatus.UNAUTHORIZED, Collections.singletonList(exception.getMessage()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        log.info("handleMethodArgumentNotValid, " + ex.getMessage());

        List<String> messages = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .collect(Collectors.toList());

        com.eleks.common.dto.ErrorDto error = createError(status, messages);
        return new ResponseEntity<>(error, headers, status);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatus status,
                                                                  WebRequest request) {
        log.info("handleHttpMessageNotReadable, " + ex.getMessage());

        String msg;
        if (ex.getCause().getCause() instanceof InvalidDateFormatException) {
            msg = ex.getCause().getCause().getMessage();
        } else if (ex.getCause() instanceof InvalidFormatException) {
            msg = ex.getCause().getMessage();
        } else {
            msg = ex.getMessage();
        }
        com.eleks.common.dto.ErrorDto error = createError(status, Collections.singletonList(msg));
        return new ResponseEntity<>(error, headers, status);
    }

    private com.eleks.common.dto.ErrorDto createError(HttpStatus status, List<String> messages) {
        return ErrorDto.builder()
                .statusCode(status.value())
                .messages(messages)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

