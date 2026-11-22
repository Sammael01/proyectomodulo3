
package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class LoginRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validLoginRequestShouldHaveNoViolations() {
        LoginRequest request = new LoginRequest(
                "daniel@gmail.com",
                "mi contraseña actual"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void blankEmailShouldGenerateViolation() {
        LoginRequest request = new LoginRequest(
                " ",
                "mi contraseña actual"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void invalidEmailShouldGenerateViolation() {
        LoginRequest request = new LoginRequest(
                "correo-invalido",
                "mi contraseña actual"
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void blankPasswordShouldGenerateViolation() {
        LoginRequest request = new LoginRequest(
                "daniel@gmail.com",
                " "
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void passwordLongerThan72CharactersShouldGenerateViolation() {
        String password = "A".repeat(73);

        LoginRequest request = new LoginRequest(
                "daniel@gmail.com",
                password
        );

        Set<ConstraintViolation<LoginRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}
