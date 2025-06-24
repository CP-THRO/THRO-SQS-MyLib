package de.throsenheim.inf.sqs.christophpircher.mylibbackend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@SecurityScheme(
        name = "Bearer Authentication",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
@OpenAPIDefinition(info = @Info(title = "MyLib Backend API", version = "v1.0"), security = @SecurityRequirement(name = "Bearer Authentication"))

@EnableAsync
@EnableScheduling
public class MyLibBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyLibBackendApplication.class, args);
    }

}
