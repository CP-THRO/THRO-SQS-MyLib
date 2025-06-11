package de.throsenheim.inf.sqs.christophpircher.mylibbackend;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "OrderService API", version = "v1.0"))
public class MyLibBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(MyLibBackendApplication.class, args);
    }

}
