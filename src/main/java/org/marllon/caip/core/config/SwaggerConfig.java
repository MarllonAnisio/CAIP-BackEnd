package org.marllon.caip.core.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    // 💡 O Toque Sênior: O ':Local' no final garante que se a variável não
    // existir no application.yml, a API NÃO VAI QUEBRAR. Ela assume "Local".
    @Value("${swagger-info.ambient:Local}")
    private String ambientType;

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Doc. CAIP - API de Reports de Itens Perdidos")
                        .description("API do sistema CAIP responsável pela gestão dos reports de itens achados e perdidos no IFPB – Campus Monteiro. \n\n**Ambiente Atual:** " + ambientType)
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
                )
                // 👇 Centralizamos a segurança do JWT de forma puramente programática
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    // 👇 Mantive a sua excelente separação de rotas!
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