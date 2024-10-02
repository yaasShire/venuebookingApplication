package com.sporton.SportOn.configuration;
//
//import io.swagger.v3.oas.annotations.OpenAPIDefinition;
//import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
//import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
//import io.swagger.v3.oas.annotations.info.Contact;
//import io.swagger.v3.oas.annotations.info.Info;
//import io.swagger.v3.oas.annotations.info.License;
//import io.swagger.v3.oas.annotations.security.SecurityRequirement;
//import io.swagger.v3.oas.annotations.security.SecurityScheme;
//import io.swagger.v3.oas.annotations.servers.Server;
//
//@OpenAPIDefinition(
//        info = @Info(
//                contact = @Contact(
//                        name = "Sport On",
//                        email = "yusufshire58@gmail.com",
//                        url = "https://aliboucoding.com/course"
//                ),
//                description = "OpenApi documentation for Spring Security",
//                title = "OpenApi specification - SportOn",
//                version = "1.0",
//                license = @License(
//                        name = "Sport On LTD",
//                        url = "https://some-url.com"
//                ),
//                termsOfService = "Terms of service"
//        ),
//        servers = {
//                @Server(
//                        description = "Local ENV",
//                        url = "http://localhost:8089/api/v1"
//                ),
//                @Server(
//                        description = "PROD ENV",
//                        url = "http://localhost:8089/api/v1"
//                )
//        },
//        security = {
//                @SecurityRequirement(
//                        name = "bearerAuth"
//                )
//        }
//)
//@SecurityScheme(
//        name = "bearerAuth",
//        description = "JWT auth description",
//        scheme = "bearer",
//        type = SecuritySchemeType.HTTP,
//        bearerFormat = "JWT",
//        in = SecuritySchemeIn.HEADER
//)
//public class OpenApiConfig {
//}

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("My API")
                        .version("1.0")
                        .description("This is my API description")
                        .contact(new Contact()
                                .name("Support Team")
                                .email("support@mycompany.com")));
    }
}
