
package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ResendVerificationRequestValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void validResendVerificationRequestShouldHaveNoViolations() {
		ResendVerificationRequest request = new ResendVerificationRequest("daniel@gmail.com");

		Set<ConstraintViolation<ResendVerificationRequest>> violations = validator.validate(request);

		assertTrue(violations.isEmpty());
	}

	@Test
	void blankEmailShouldGenerateViolation() {
		ResendVerificationRequest request = new ResendVerificationRequest(" ");

		Set<ConstraintViolation<ResendVerificationRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void invalidEmailShouldGenerateViolation() {
		ResendVerificationRequest request = new ResendVerificationRequest("correo-invalido");

		Set<ConstraintViolation<ResendVerificationRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void emailLongerThan150CharactersShouldGenerateViolation() {
		String email = "a".repeat(140) + "@gmail.com";

		ResendVerificationRequest request = new ResendVerificationRequest(email);

		Set<ConstraintViolation<ResendVerificationRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}
}
