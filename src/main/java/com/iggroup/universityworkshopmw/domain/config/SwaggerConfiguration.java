package com.iggroup.universityworkshopmw.domain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import static springfox.documentation.builders.PathSelectors.any;
import static springfox.documentation.builders.RequestHandlerSelectors.basePackage;
import static springfox.documentation.spi.DocumentationType.SWAGGER_2;

@Configuration
@EnableSwagger2
public class SwaggerConfiguration {

   @Bean
   public Docket api() {
      return new Docket(SWAGGER_2)
            .select()
            .apis(basePackage("com.iggroup.universityworkshopmw.integration.controllers"))
            .paths(any())
            .build()
            .apiInfo(apiInfo());
   }

   private ApiInfo apiInfo() {
      return new ApiInfoBuilder()
            .title("University workshop API")
            .description("The API for the university workshop middleware application")
            .version("1.0")
            .contact(new Contact("University Workshop", "https://github.com/IGLearning/UniversityWorkshopMw", "prototypeservice@ig.com"))
            .build();
   }
}
