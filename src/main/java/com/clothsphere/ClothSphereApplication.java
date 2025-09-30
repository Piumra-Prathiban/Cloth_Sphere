package com.clothsphere;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan("com.clothsphere.model")
@EnableJpaRepositories("com.clothsphere.repository")
public class ClothSphereApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClothSphereApplication.class, args);
        System.out.println("✅ ClothSphere Production Management Started Successfully!");
        System.out.println("✅ Server running on: http://localhost:8080");
    }
}