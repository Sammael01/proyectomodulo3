
package co.edu.unbosque.tstyle.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

/**
 * Configura la documentación OpenAPI de la aplicación T Style.
 *
 * Define la información general de la API y el esquema de autenticación
 * mediante tokens JWT.
 *
 * @author Equipo T Style
 * @version 1.0
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "T Style API", version = "1.0.0", description = "Documentación de los servicios backend de T Style."))
@SecurityScheme(name = "bearerAuth", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT")
public class OpenApiConfig {

}
