
package co.edu.unbosque.tstyle.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import co.edu.unbosque.tstyle.security.filter.JwtAuthenticationFilter;
import co.edu.unbosque.tstyle.security.service.JwtService;
import co.edu.unbosque.tstyle.user.repository.UserRepository;

/**
 * Configura los mecanismos de seguridad de la aplicación T Style.
 *
 * Define los endpoints públicos, las rutas protegidas, la gestión
 * de sesiones y el control de acceso mediante autenticación JWT
 * y roles de usuario.
 *
 * @author Equipo T Style
 * @version 1.0
 */
@Configuration
public class SecurityConfig {

    /**
     * Configura la cadena de filtros de seguridad de la aplicación.
     *
     * Permite el acceso público al registro, la verificación del correo,
     * el inicio de sesión y la recuperación de contraseña. Restringe
     * las rutas administrativas al rol ADMIN y exige autenticación
     * para las demás solicitudes.
     *
     * @param http configuración de seguridad HTTP
     * @param jwtAuthenticationFilter filtro de autenticación JWT
     * @return cadena de filtros de seguridad configurada
     * @throws Exception si ocurre un error durante la configuración
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {

        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .formLogin(AbstractHttpConfigurer::disable)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"status\":401,\"error\":\"UNAUTHORIZED\",\"message\":\"Se requiere autenticación\"}"
                    );
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"status\":403,\"error\":\"FORBIDDEN\",\"message\":\"No tienes permisos para acceder a este recurso\"}"
                    );
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    HttpMethod.POST,
                    "/api/users/register",
                    "/api/users/verify-email",
                    "/api/users/resend-verification",
                    "/api/auth/login",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password"
                ).permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }

    /**
     * Crea el filtro encargado de procesar los tokens JWT.
     *
     * @param jwtService servicio encargado de generar y validar tokens JWT
     * @param userRepository repositorio utilizado para consultar los usuarios
     * @return filtro de autenticación JWT configurado
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(
            JwtService jwtService,
            UserRepository userRepository) {

        return new JwtAuthenticationFilter(jwtService, userRepository);
    }

    /**
     * Evita que el filtro JWT sea registrado dos veces por el contenedor
     * de servlets y por Spring Security.
     *
     * @param filter filtro de autenticación JWT
     * @return configuración del registro del filtro
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration(
            JwtAuthenticationFilter filter) {

        FilterRegistrationBean<JwtAuthenticationFilter> registration =
                new FilterRegistrationBean<>(filter);

        registration.setEnabled(false);

        return registration;
    }
}
