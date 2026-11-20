package co.edu.unbosque.tstyle.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;

/**
 * Pruebas de los manejadores de errores agregados para los módulos de
 * carrito, pedidos y administración (401, 403 y errores 4xx de Spring MVC).
 *
 * @author Equipo T Style
 */
class GlobalExceptionHandlerSecurityTest {

    private GlobalExceptionHandler handler;
    private WebRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(WebRequest.class);

        when(request.getDescription(false)).thenReturn("uri=/api/test");
    }

    @Test
    void handleUnauthorizedExceptionReturns401() {

        ResponseEntity<ErrorResponse> response = handler.handleUnauthorizedException(
                new UnauthorizedException("Debes iniciar sesión"), request);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("UNAUTHORIZED", response.getBody().getError());
        assertEquals("Debes iniciar sesión", response.getBody().getMessage());
        assertEquals("/api/test", response.getBody().getPath());
    }

    @Test
    void handleForbiddenExceptionReturns403() {

        ResponseEntity<ErrorResponse> response = handler.handleForbiddenException(
                new ForbiddenException("Solo administradores"), request);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("FORBIDDEN", response.getBody().getError());
    }

    @Test
    void springClientErrorsKeepTheirStatusInsteadOfBecoming500() {

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(
                new ResponseStatusException(HttpStatus.NOT_FOUND), request);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("NOT_FOUND", response.getBody().getError());
        assertEquals("El recurso solicitado no existe", response.getBody().getMessage());
    }

    @Test
    void methodNotAllowedIsReportedAs405() {

        ResponseEntity<ErrorResponse> response = handler.handleGeneralException(
                new ResponseStatusException(HttpStatus.METHOD_NOT_ALLOWED), request);

        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertEquals("METHOD_NOT_ALLOWED", response.getBody().getError());
    }
}
