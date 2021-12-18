package com.example.sap_project;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAsync(proxyTargetClass = true)
@EnableTransactionManagement
public class SapProjectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SapProjectApplication.class, args);
    }
}
