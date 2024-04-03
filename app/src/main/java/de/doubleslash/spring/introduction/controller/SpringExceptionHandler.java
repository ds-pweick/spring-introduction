package de.doubleslash.spring.introduction.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import io.minio.errors.MinioException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class SpringExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(CarNotFoundException.class)
    public ResponseEntity<Object> handleCarNotFoundException(CarNotFoundException e, WebRequest req) {
        log.error(e.getMessage());
        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.NOT_FOUND, req);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<Object> handleJsonProcessingException(JsonProcessingException e, WebRequest req) {
        log.error(e.getMessage());
        return handleExceptionInternal(e, CarDealershipService.CAR_JSON_PARSE_FAILURE_STRING, new HttpHeaders(),
                HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(CarModelAndOrBrandStringInvalidException.class)
    public ResponseEntity<Object> handleCarModelAndOrBrandStringInvalidException(
            CarModelAndOrBrandStringInvalidException e, WebRequest req) {
        log.error(e.getMessage());
        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(InvalidFileRequestException.class)
    public ResponseEntity<Object> handleInvalidFileRequestException(InvalidFileRequestException e, WebRequest req) {
        log.error(e.getMessage());
        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(MinioException.class)
    public ResponseEntity<Object> handleMinioException(MinioException e, WebRequest req) {
        log.error(e.getMessage());
        return handleExceptionInternal(e, e.getMessage(), new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
    }
}

