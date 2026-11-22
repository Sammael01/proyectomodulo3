package co.edu.unbosque.tstyle.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import co.edu.unbosque.tstyle.exception.ForbiddenException;
import co.edu.unbosque.tstyle.exception.UnauthorizedException;
import co.edu.unbosque.tstyle.user.enums.Role;
import co.edu.unbosque.tstyle.user.model.User;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

@Service
public class CurrentUserService {
	private final UserRepository userRepository;

	public CurrentUserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public User requireUser() {
		User user = userRepository.findByEmail(resolveEmail())
				.orElseThrow(() -> new UnauthorizedException("Usuario no autenticado"));

		if (!user.isEnabled()) {
			throw new ForbiddenException("La cuenta del usuario está deshabilitada");
		}

		return user;
	}

	public User requireAdmin() {
		User user = requireUser();

		if (user.getRole() != Role.ADMIN) {
			throw new ForbiddenException("Esta operación es solo para administradores");
		}

		return user;
	}

	private String resolveEmail() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()
				|| authentication instanceof AnonymousAuthenticationToken
				|| authentication.getName() == null || authentication.getName().isBlank()) {
			throw new UnauthorizedException("Usuario no autenticado");
		}

		return authentication.getName();
	}
}
