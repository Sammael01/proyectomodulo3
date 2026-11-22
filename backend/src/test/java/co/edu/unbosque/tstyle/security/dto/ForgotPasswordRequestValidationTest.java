
package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ForgotPasswordRequestValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void validForgotPasswordRequestShouldHaveNoViolations() {
		ForgotPasswordRequest request = new ForgotPasswordRequest("daniel@gmail.com");

		Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

		assertTrue(violations.isEmpty());
	}

	@Test
	void blankEmailShouldGenerateViolation() {
		ForgotPasswordRequest request = new ForgotPasswordRequest(" ");

		Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void invalidEmailShouldGenerateViolation() {
		ForgotPasswordRequest request = new ForgotPasswordRequest("correo-invalido");

		Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void emailLongerThan150CharactersShouldGenerateViolation() {
		String email = "a".repeat(140) + "@gmail.com";

		ForgotPasswordRequest request = new ForgotPasswordRequest(email);

		Set<ConstraintViolation<ForgotPasswordRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}
}
