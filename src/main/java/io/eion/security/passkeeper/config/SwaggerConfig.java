package io.eion.security.passkeeper.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author <a href="joelin@digitalriver.com">Joe Lin</a>
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("io.eion.security.passkeeper.web"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(this.apiInfo());
    }

    private ApiInfo apiInfo() {
        final Contact contact = new Contact("Joe", null, "tingjan1982@gmail.com");
        ApiInfo apiInfo = new ApiInfo(
                "Password Keeper",
                "REST API for password management.",
                "0.1.0",
                "Terms of service",
                contact,
                "Apache 2 Licence",
                "https://github.com/tingjan1982/password-keeper/blob/master/LICENSE");

        return apiInfo;
    }
}
