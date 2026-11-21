package co.edu.unbosque.tstyle.security;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import co.edu.unbosque.tstyle.exception.ForbiddenException;
import co.edu.unbosque.tstyle.exception.UnauthorizedException;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {
	@Mock
	private UserRepository userRepository;

	private CurrentUserService currentUserService;

	private User client;

	@BeforeEach
	void setUp() {
		currentUserService = new CurrentUserService(userRepository);
		client = new User("Ana Pérez", "ana@tstyle.com", "hash", Role.CLIENT);
		client.setId(1L);
	}

	@AfterEach
	void tearDown() {
		SecurityContextHolder.clearContext();
	}

	private void authenticate(String email, Role role) {
		SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
				email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role.name()))));
	}

	@Test
	void requireUserReadsTheAuthenticatedEmail() {
		authenticate("ana@tstyle.com", Role.CLIENT);
		when(userRepository.findByEmail("ana@tstyle.com")).thenReturn(Optional.of(client));

		assertSame(client, currentUserService.requireUser());
	}

	@Test
	void requireUserFailsWithoutAuthentication() {
		assertThrows(UnauthorizedException.class, () -> currentUserService.requireUser());
	}

	@Test
	void requireUserFailsForAnonymousAuthentication() {
		SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
				"key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))));

		assertThrows(UnauthorizedException.class, () -> currentUserService.requireUser());
	}

	@Test
	void requireUserFailsWhenTheUserNoLongerExists() {
		authenticate("ana@tstyle.com", Role.CLIENT);
		when(userRepository.findByEmail("ana@tstyle.com")).thenReturn(Optional.empty());

		assertThrows(UnauthorizedException.class, () -> currentUserService.requireUser());
	}

	@Test
	void requireUserRejectsDisabledAccounts() {
		client.setEnabled(false);
		authenticate("ana@tstyle.com", Role.CLIENT);
		when(userRepository.findByEmail("ana@tstyle.com")).thenReturn(Optional.of(client));

		assertThrows(ForbiddenException.class, () -> currentUserService.requireUser());
	}

	@Test
	void requireAdminRejectsClients() {
		authenticate("ana@tstyle.com", Role.CLIENT);
		when(userRepository.findByEmail("ana@tstyle.com")).thenReturn(Optional.of(client));

		assertThrows(ForbiddenException.class, () -> currentUserService.requireAdmin());
	}

	@Test
	void requireAdminAcceptsAdministrators() {
		User admin = new User("Admin", "admin@tstyle.com", "hash", Role.ADMIN);
		admin.setId(2L);
		authenticate("admin@tstyle.com", Role.ADMIN);
		when(userRepository.findByEmail("admin@tstyle.com")).thenReturn(Optional.of(admin));

		assertSame(admin, currentUserService.requireAdmin());
	}
}
