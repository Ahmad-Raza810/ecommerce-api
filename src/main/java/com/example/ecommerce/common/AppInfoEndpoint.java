package com.example.ecommerce.common;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Endpoint(id = "app-info")
public class AppInfoEndpoint {

    @ReadOperation
    public Map<String, Object> appInfo() {
        return Map.of(
                "application", "RecipeHub",
                "version", "1.0",
                "environment", "dev"
        );
    }
}
