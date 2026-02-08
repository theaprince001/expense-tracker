package com.expensetracker.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Expense Tracker API",
                version = "1.0.0",
                description = """
            #  Personal Finance Management API
            
            A complete REST API for tracking expenses, managing budgets, and generating financial reports.
            
            ##  Authentication
            - Register → Activate via email → Login → Use JWT token
            - Add `Authorization: Bearer {token}` to all requests
            
            ##  Key Features
            - Transaction CRUD with receipt upload
            - Budget management with alerts
            - Financial dashboard & analytics
            - PDF/CSV report generation
            - Admin user management
            """,
                contact = @Contact(
                        name = "Expense Tracker Team",
                        email = "support@expensetracker.com"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                ),
                termsOfService = "https://expensetracker.com/terms"
        ),
        servers = {
                @Server(
                        url = "http://localhost:8080",
                        description = "Local Development Server"
                ),
                @Server(
                        url = "https://api.expensetracker.com",
                        description = "Production Server"
                )
        },
        security = {
                @SecurityRequirement(name = "bearerAuth")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        description = "JWT Authorization header using the Bearer scheme."
)
public class OpenApiConfig {

}