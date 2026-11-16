package co.edu.unbosque.tstyle.security;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import co.edu.unbosque.tstyle.exception.ForbiddenException;
import co.edu.unbosque.tstyle.exception.UnauthorizedException;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Pruebas unitarias para CurrentUserService.
 *
 * @author Equipo T Style
 */
@ExtendWith(MockitoExtension.class)
class CurrentUserServiceTest {

    @Mock
    private UserRepository userRepository;

    private User client;

    @BeforeEach
    void setUp() {
        client = new User("Ana Pérez", "ana@tstyle.com", "hash", Role.CLIENT);
        client.setId(1L);
    }

    @AfterEach
    void clearRequest() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void requestWithUserHeader(String value) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CurrentUserService.DEV_USER_HEADER, value);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void requireUserIgnoresHeaderWhenDevModeIsDisabled() {

        CurrentUserService service = new CurrentUserService(userRepository, false);
        requestWithUserHeader("1");

        assertThrows(UnauthorizedException.class, service::requireUser);
    }

    @Test
    void requireUserReadsHeaderWhenDevModeIsEnabled() {

        CurrentUserService service = new CurrentUserService(userRepository, true);
        requestWithUserHeader("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        assertSame(client, service.requireUser());
    }

    @Test
    void requireUserFailsWithoutHeader() {

        CurrentUserService service = new CurrentUserService(userRepository, true);
        RequestContextHolder.setRequestAttributes(
                new ServletRequestAttributes(new MockHttpServletRequest()));

        assertThrows(UnauthorizedException.class, service::requireUser);
    }

    @Test
    void requireUserRejectsDisabledAccounts() {

        CurrentUserService service = new CurrentUserService(userRepository, true);
        client.setEnabled(false);
        requestWithUserHeader("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThrows(ForbiddenException.class, service::requireUser);
    }

    @Test
    void requireAdminRejectsClients() {

        CurrentUserService service = new CurrentUserService(userRepository, true);
        requestWithUserHeader("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(client));

        assertThrows(ForbiddenException.class, service::requireAdmin);
    }

    @Test
    void requireAdminAcceptsAdministrators() {

        CurrentUserService service = new CurrentUserService(userRepository, true);
        User admin = new User("Admin", "admin@tstyle.com", "hash", Role.ADMIN);
        admin.setId(2L);
        requestWithUserHeader("2");
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));

        assertSame(admin, service.requireAdmin());
    }
}
