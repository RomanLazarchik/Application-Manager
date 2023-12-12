package roman.lazarchik.ApplicationManager.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.validation.ObjectError;
import roman.lazarchik.ApplicationManager.dto.ErrorResponse;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleApplicationNotFoundException(ApplicationNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getMessage(), "Application not found");
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidApplicationStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidApplicationStatusException(InvalidApplicationStatusException ex) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getMessage(), "Invalid Application Status");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApplicationAlreadyPublishedException.class)
    public ResponseEntity<ErrorResponse> handleApplicationAlreadyPublishedException(ApplicationAlreadyPublishedException ex) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getMessage(), "Application Already Published");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ContentEditNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleContentEditNotAllowedException(ContentEditNotAllowedException ex) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getMessage(), "Content Edit Not Allowed");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(InvalidInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInputException(InvalidInputException ex) {
        ErrorResponse error = new ErrorResponse(LocalDateTime.now(), ex.getMessage(), "Invalid Input");
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        String errorMessages = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        ErrorResponse errorResponse = new ErrorResponse(LocalDateTime.now(), "Validation Error", errorMessages);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }
}

