package de.doubleslash.spring.introduction.controller;


import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class ExceptionHandler extends ResponseEntityExceptionHandler {
    @org.springframework.web.bind.annotation.ExceptionHandler(CarNotFoundException.class)
    public ResponseEntity<Object> handleCarModelOrBrandStringTooLongException(CarNotFoundException e, WebRequest req) {
        log.error(e.getMessage());
        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, req);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(CarModelOrBrandStringInvalidException.class)
    public ResponseEntity<Object> handleCarModelOrBrandStringTooLongException(CarModelOrBrandStringInvalidException e, WebRequest req) {
        log.error(e.getMessage());
        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(InvalidFileRequestException.class)
    public ResponseEntity<Object> handleInvalidFileRequestException(InvalidFileRequestException e, WebRequest req) {
        log.error(e.getMessage());
        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
    }

    @org.springframework.web.bind.annotation.ExceptionHandler(MinioException.class)
    public ResponseEntity<Object> handleMinioException(CarModelOrBrandStringInvalidException e, WebRequest req) {
        log.error(e.getMessage());
        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
    }
}

