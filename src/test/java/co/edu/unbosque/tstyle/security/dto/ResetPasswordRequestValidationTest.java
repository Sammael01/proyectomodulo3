
package co.edu.unbosque.tstyle.security.dto;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

class ResetPasswordRequestValidationTest {

	private Validator validator;

	@BeforeEach
	void setUp() {
		ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
		validator = factory.getValidator();
	}

	@Test
	void validResetPasswordRequestShouldHaveNoViolations() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "123456", "Daniel123*");

		Set<ConstraintViolation<ResetPasswordRequest>> violations = validator.validate(request);

		assertTrue(violations.isEmpty());
	}

	@Test
	void blankEmailShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest(" ", "123456", "Daniel123*");

		assertTrue(validator.validate(request).stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void invalidEmailShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("correo-invalido", "123456", "Daniel123*");

		assertTrue(validator.validate(request).stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void emailLongerThan150CharactersShouldGenerateViolation() {
		String email = "a".repeat(140) + "@gmail.com";

		ResetPasswordRequest request = new ResetPasswordRequest(email, "123456", "Daniel123*");

		assertTrue(validator.validate(request).stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
	}

	@Test
	void blankCodeShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", " ", "Daniel123*");

		assertTrue(validator.validate(request).stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")));
	}

	@Test
	void codeWithLessThanSixDigitsShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "12345", "Daniel123*");

		assertTrue(validator.validate(request).stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")));
	}

	@Test
	void codeWithLettersShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "12AB56", "Daniel123*");

		assertTrue(validator.validate(request).stream().anyMatch(v -> v.getPropertyPath().toString().equals("code")));
	}

	@Test
	void blankPasswordShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "123456", " ");

		assertTrue(validator.validate(request).stream()
				.anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
	}

	@Test
	void passwordShorterThanEightCharactersShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "123456", "Dan1*");

		assertTrue(validator.validate(request).stream()
				.anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
	}

	@Test
	void passwordWithoutUppercaseShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "123456", "daniel123*");

		assertTrue(validator.validate(request).stream()
				.anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
	}

	@Test
	void passwordWithoutLowercaseShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "123456", "DANIEL123*");

		assertTrue(validator.validate(request).stream()
				.anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
	}

	@Test
	void passwordWithoutNumberShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "123456", "Danielabc*");

		assertTrue(validator.validate(request).stream()
				.anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
	}

	@Test
	void passwordWithoutSymbolShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "123456", "Daniel123");

		assertTrue(validator.validate(request).stream()
				.anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
	}

	@Test
	void passwordWithSpacesShouldGenerateViolation() {
		ResetPasswordRequest request = new ResetPasswordRequest("daniel@gmail.com", "123456", "Daniel 123*");

		assertTrue(validator.validate(request).stream()
				.anyMatch(v -> v.getPropertyPath().toString().equals("newPassword")));
	}
}
