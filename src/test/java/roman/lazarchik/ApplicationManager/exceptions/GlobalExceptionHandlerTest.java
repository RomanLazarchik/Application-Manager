package roman.lazarchik.ApplicationManager.exceptions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import roman.lazarchik.ApplicationManager.dto.ErrorResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private InvalidApplicationStatusException invalidApplicationStatusException;
    @Mock
    private ApplicationNotFoundException applicationNotFoundException;
    @Mock
    private ApplicationAlreadyExistsException applicationAlreadyPublishedException;
    @Mock
    private ContentEditNotAllowedException contentEditNotAllowedException;
    @Mock
    private InvalidInputException invalidInputException;
    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult("object", "objectName");
        bindingResult.addError(new ObjectError("objectName", "Default Message"));

        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
    }

    @Test
    void testHandleApplicationNotFoundException() {

        String errorMessage = "Application not found";
        when(applicationNotFoundException.getMessage()).thenReturn(errorMessage);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleApplicationNotFoundException(applicationNotFoundException);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getDetails()).isEqualTo("Application not found");

        verify(applicationNotFoundException, times(1)).getMessage();
    }

    @Test
    void testHandleInvalidApplicationStatusException() {

        String errorMessage = "Invalid Application Status";
        when(invalidApplicationStatusException.getMessage()).thenReturn(errorMessage);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidApplicationStatusException(invalidApplicationStatusException);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getDetails()).isEqualTo("Invalid Application Status");

        verify(invalidApplicationStatusException, times(1)).getMessage();
    }

//    @Test
//    void testHandleApplicationAlreadyPublishedException() {
//
//        String errorMessage = "Application Already Published";
//        when(applicationAlreadyPublishedException.getMessage()).thenReturn(errorMessage);
//
//        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleApplicationAlreadyPublishedException(applicationAlreadyPublishedException);
//
//        assertThat(response).isNotNull();
//        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
//        assertThat(response.getBody()).isNotNull();
//        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
//        assertThat(response.getBody().getDetails()).isEqualTo("Application Already Published");
//
//        verify(applicationAlreadyPublishedException, times(1)).getMessage();
//    }

    @Test
    void testHandleContentEditNotAllowedException() {

        String errorMessage = "Content Edit Not Allowed";
        when(contentEditNotAllowedException.getMessage()).thenReturn(errorMessage);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleContentEditNotAllowedException(contentEditNotAllowedException);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getDetails()).isEqualTo("Content Edit Not Allowed");

        verify(contentEditNotAllowedException, times(1)).getMessage();
    }

    @Test
    void testHandleInvalidInputException() {

        String errorMessage = "Invalid Input";
        when(invalidInputException.getMessage()).thenReturn(errorMessage);

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleInvalidInputException(invalidInputException);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo(errorMessage);
        assertThat(response.getBody().getDetails()).isEqualTo("Invalid Input");

        verify(invalidInputException, times(1)).getMessage();
    }

    @Test
    void testHandleMethodArgumentNotValid() {

        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleMethodArgumentNotValid(methodArgumentNotValidException);

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Validation Error");
        assertThat(response.getBody().getDetails()).contains("Default Message");
    }
}

