package org.marllon.caip.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class SwaggerConfig {

    @Value("${swagger-info.server-url}")
    private String serverUrl;

    @Value("${swagger-info.ambient}")
    private String ambientType;

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .servers(List.of(
                        new Server().url(serverUrl).description(ambientType)
                ))
                .info(new Info()
                        .title("Doc. CAIP - API de Reports de Itens Perdidos")
                        .description("API do sistema CAIP responsável pela gestão dos reports de itens achados e perdidos no IFPB – Campus Monteiro.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Marllon Anisio")
                                .email("marllon.anizio@gmail.com")
                                .url("https://www.linkedin.com/in/marllon-anisio/")
                        )
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")
                        )
                );
    }

    @Bean
    public GroupedOpenApi publicApiGroup() {
        return GroupedOpenApi.builder()
                .group("api-public-endpoints")
                .pathsToMatch("/**")
                .pathsToExclude("/actuator/**")
                .displayName("API - Endpoints da Aplicação")
                .build();
    }

    @Bean
    public GroupedOpenApi apiActuatorGroup() {
        return GroupedOpenApi.builder()
                .group("api-private-monitoring")
                .pathsToMatch("/actuator/**")
                .addOpenApiCustomizer(openApi -> {
                    openApi.setTags(List.of(
                            new Tag()
                                    .name("Actuator")
                                    .description("Documentação dos endpoints de monitoramento e saúde da aplicação.")
                    ));
                    openApi.externalDocs(new ExternalDocumentation()
                            .description("Documentação Oficial do Actuator")
                            .url("https://docs.spring.io/spring-boot/api/rest/actuator/index.html")
                    );
                })
                .displayName("API - Monitoramento Interno")
                .build();
    }
}