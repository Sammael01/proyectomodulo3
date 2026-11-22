
package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class VerifyEmailRequestValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void validVerifyEmailRequestShouldHaveNoViolations() {
		VerifyEmailRequest request = new VerifyEmailRequest("daniel@gmail.com", "123456");

		Set<ConstraintViolation<VerifyEmailRequest>> violations = validator.validate(request);

		assertTrue(violations.isEmpty());
	}

	@Test
	void blankEmailShouldGenerateViolation() {
		VerifyEmailRequest request = new VerifyEmailRequest(" ", "123456");

		Set<ConstraintViolation<VerifyEmailRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void invalidEmailShouldGenerateViolation() {
		VerifyEmailRequest request = new VerifyEmailRequest("correo-invalido", "123456");

		Set<ConstraintViolation<VerifyEmailRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void emailLongerThan150CharactersShouldGenerateViolation() {
		String email = "a".repeat(140) + "@gmail.com";

		VerifyEmailRequest request = new VerifyEmailRequest(email, "123456");

		Set<ConstraintViolation<VerifyEmailRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void blankCodeShouldGenerateViolation() {
		VerifyEmailRequest request = new VerifyEmailRequest("daniel@gmail.com", " ");

		Set<ConstraintViolation<VerifyEmailRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")));
	}

	@Test
	void codeWithLessThanSixDigitsShouldGenerateViolation() {
		VerifyEmailRequest request = new VerifyEmailRequest("daniel@gmail.com", "12345");

		Set<ConstraintViolation<VerifyEmailRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")));
	}

	@Test
	void codeWithLettersShouldGenerateViolation() {
		VerifyEmailRequest request = new VerifyEmailRequest("daniel@gmail.com", "12AB56");

		Set<ConstraintViolation<VerifyEmailRequest>> violations = validator.validate(request);

		assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")));
	}
}
