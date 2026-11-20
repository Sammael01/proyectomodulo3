package co.edu.unbosque.tstyle.exception;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Centraliza el manejo de excepciones generadas
 * durante las solicitudes HTTP de T Style.
 *
 * @author Equipo T Style
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja errores relacionados con reglas de negocio.
     *
     * @param exception excepción generada
     * @param request solicitud HTTP
     * @return respuesta HTTP con el error correspondiente
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception, WebRequest request) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "BUSINESS_ERROR",
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja solicitudes de recursos inexistentes.
     *
     * @param exception excepción generada
     * @param request solicitud HTTP
     * @return respuesta HTTP con el error correspondiente
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception, WebRequest request) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.NOT_FOUND.value(),
                "NOT_FOUND",
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    /**
     * Maneja conflictos ocasionados por recursos duplicados.
     *
     * @param exception excepción generada
     * @param request solicitud HTTP
     * @return respuesta HTTP con el error correspondiente
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException exception, WebRequest request) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.CONFLICT.value(),
                "CONFLICT",
                exception.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    /**
     * Maneja errores producidos por validaciones de los DTO.
     *
     * @param exception excepción de validación
     * @param request solicitud HTTP
     * @return respuesta HTTP con el primer error de validación
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception, WebRequest request) {

        String message = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .orElse("Los datos enviados no son válidos");

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                "VALIDATION_ERROR",
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Maneja solicitudes de usuarios que no han iniciado sesión.
     *
     * @param exception excepción generada
     * @param request solicitud HTTP
     * @return respuesta HTTP 401
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorizedException(
            UnauthorizedException exception, WebRequest request) {

        return buildResponse(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
                exception.getMessage(), request);
    }

    /**
     * Maneja solicitudes de usuarios sin permisos suficientes.
     *
     * @param exception excepción generada
     * @param request solicitud HTTP
     * @return respuesta HTTP 403
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(
            ForbiddenException exception, WebRequest request) {

        return buildResponse(HttpStatus.FORBIDDEN, "FORBIDDEN",
                exception.getMessage(), request);
    }

    /**
     * Maneja cuerpos JSON mal formados o con valores no válidos
     * (por ejemplo, un enum inexistente).
     *
     * @param exception excepción generada
     * @param request solicitud HTTP
     * @return respuesta HTTP 400
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableBody(
            HttpMessageNotReadableException exception, WebRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "El cuerpo de la solicitud no es válido o está mal formado", request);
    }

    /**
     * Maneja parámetros de URL con un tipo o valor no válido.
     *
     * @param exception excepción generada
     * @param request solicitud HTTP
     * @return respuesta HTTP 400
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception, WebRequest request) {

        return buildResponse(HttpStatus.BAD_REQUEST, "BAD_REQUEST",
                "El valor del parámetro '" + exception.getName() + "' no es válido", request);
    }

    /**
     * Maneja errores inesperados que no tienen un tratamiento específico.
     *
     * @param exception excepción generada
     * @param request solicitud HTTP
     * @return respuesta HTTP genérica
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception exception, WebRequest request) {

        // Errores 4xx propios de Spring MVC (ruta inexistente, método no permitido,
        // parámetro faltante...) conservan su código en lugar de convertirse en 500.
        if (exception instanceof org.springframework.web.ErrorResponse springError
                && springError.getStatusCode().is4xxClientError()) {

            HttpStatus status = HttpStatus.resolve(springError.getStatusCode().value());
            if (status == null) {
                status = HttpStatus.BAD_REQUEST;
            }
            return buildResponse(status,
                    status.name(),
                    status == HttpStatus.NOT_FOUND
                            ? "El recurso solicitado no existe"
                            : "La solicitud no es válida",
                    request);
        }

        LOGGER.error("Error inesperado procesando la solicitud", exception);

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "INTERNAL_SERVER_ERROR",
                "Ocurrió un error interno en el servidor",
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    private ResponseEntity<ErrorResponse> buildResponse(
            HttpStatus status, String error, String message, WebRequest request) {

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                message,
                request.getDescription(false).replace("uri=", "")
        );

        return ResponseEntity.status(status).body(response);
    }
}