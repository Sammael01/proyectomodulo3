
package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class RegisterRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void validRegisterRequestShouldHaveNoViolations() {
        RegisterRequest request = new RegisterRequest(
                "Daniel Zapata",
                "daniel@gmail.com",
                "Daniel123!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void blankNameShouldGenerateViolation() {
        RegisterRequest request = new RegisterRequest(
                " ",
                "daniel@gmail.com",
                "Daniel123!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void invalidEmailShouldGenerateViolation() {
        RegisterRequest request = new RegisterRequest(
                "Daniel Zapata",
                "correo-invalido",
                "Daniel123!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void shortPasswordShouldGenerateViolation() {
        RegisterRequest request = new RegisterRequest(
                "Daniel Zapata",
                "daniel@gmail.com",
                "Dan1!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void passwordWithoutUppercaseShouldGenerateViolation() {
        RegisterRequest request = new RegisterRequest(
                "Daniel Zapata",
                "daniel@gmail.com",
                "daniel123!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @Test
    void passwordWithSpacesShouldGenerateViolation() {
        RegisterRequest request = new RegisterRequest(
                "Daniel Zapata",
                "daniel@gmail.com",
                "Daniel 123!"
        );

        Set<ConstraintViolation<RegisterRequest>> violations =
                validator.validate(request);

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }
}
