package com.harshit.monocept.config;

import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@Configuration
@OpenAPIDefinition(info = @Info(title = "Insurance Policy & Claim Management System", version = "1.0.0", description = "REST API for managing insurance products, "
		+ "policies, payments and claims with JWT security. " + "Login first to get token, then click Authorize."))
@SecurityScheme(name = "Bearer Authentication", type = SecuritySchemeType.HTTP, scheme = "bearer", bearerFormat = "JWT", description = "Enter JWT token from /api/auth/login response")
public class SwaggerConfig {

}