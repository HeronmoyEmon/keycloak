package com.northwindapps.billing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Northwind Apps billing/invoicing API -- the chapter 34 lab's Spring Boot OAuth2 Resource
 * Server. See {@link SecurityConfig} for the JWT validation and authorization wiring, and
 * {@link BillingController} for the three endpoints this chapter curls at three different
 * protection levels.
 */
@SpringBootApplication
public class BillingApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BillingApiApplication.class, args);
    }
}
