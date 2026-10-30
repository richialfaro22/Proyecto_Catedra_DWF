package com.udb.hotel.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentación de la API: http://localhost:8080/swagger-ui.html
 * Usa el botón "Authorize" y pega el token obtenido en POST /auth/login.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI hotelOpenApi() {
        final String esquema = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("API REST - Sistema de reservas de hotel")
                        .version("1.0.0")
                        .description("Administración de hoteles, habitaciones, tarifas, servicios, huéspedes, "
                                + "reservas, pagos, reclamos y estadísticas. Proyecto de cátedra DWF - UDB."))
                .addSecurityItem(new SecurityRequirement().addList(esquema))
                .components(new Components().addSecuritySchemes(esquema, new SecurityScheme()
                        .name(esquema)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}
