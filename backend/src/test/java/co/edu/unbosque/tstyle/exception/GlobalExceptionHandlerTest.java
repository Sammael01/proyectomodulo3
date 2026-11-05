package co.edu.unbosque.tstyle.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(WebRequest.class);

        when(request.getDescription(false))
                .thenReturn("uri=/api/test");
    }

    @Test
    void handleBusinessExceptionReturnsBadRequest() {
        BusinessException exception =
                new BusinessException("Regla de negocio incumplida");

        ResponseEntity<ErrorResponse> response =
                handler.handleBusinessException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("BUSINESS_ERROR", response.getBody().getError());
        assertEquals("Regla de negocio incumplida",
                response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
        assertNotNull(response.getBody().getTimestamp());
    }

    @Test
    void handleResourceNotFoundExceptionReturnsNotFound() {
        ResourceNotFoundException exception =
                new ResourceNotFoundException("Usuario no encontrado");

        ResponseEntity<ErrorResponse> response =
                handler.handleResourceNotFoundException(exception, request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", response.getBody().getError());
        assertEquals("Usuario no encontrado",
                response.getBody().getMessage());
    }

    @Test
    void handleDuplicateResourceExceptionReturnsConflict() {
        DuplicateResourceException exception =
                new DuplicateResourceException("El recurso ya existe");

        ResponseEntity<ErrorResponse> response =
                handler.handleDuplicateResourceException(exception, request);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CONFLICT", response.getBody().getError());
        assertEquals("El recurso ya existe",
                response.getBody().getMessage());
    }

    @Test
    void handleValidationExceptionReturnsBadRequest() {
        BindingResult bindingResult =
                new BeanPropertyBindingResult(new Object(), "registerRequest");

        bindingResult.addError(
                new FieldError(
                        "registerRequest",
                        "email",
                        "El correo no tiene un formato válido"
                )
        );

        MethodArgumentNotValidException exception =
                new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorResponse> response =
                handler.handleValidationException(exception, request);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().getError());
        assertEquals("email: El correo no tiene un formato válido",
                response.getBody().getMessage());
    }

    @Test
    void handleGeneralExceptionReturnsInternalServerError() {
        Exception exception = new Exception("Error interno de prueba");

        ResponseEntity<ErrorResponse> response =
                handler.handleGeneralException(exception, request);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR,
                response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR",
                response.getBody().getError());
        assertEquals("Ocurrió un error interno en el servidor",
                response.getBody().getMessage());
    }
}