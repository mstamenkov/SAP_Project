package com.example.sap_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
public class SapProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapProjectApplication.class, args);
    }
}
