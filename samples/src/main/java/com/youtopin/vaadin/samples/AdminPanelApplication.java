package com.youtopin.vaadin.samples;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point for the Admin Panel Spring Boot application.
 *
 * <p>
 * This class boots the embedded Spring Boot container and initializes the
 * Vaadin servlet.  It is marked with {@link SpringBootApplication} which
 * combines several annotations including {@code @Configuration},
 * {@code @EnableAutoConfiguration} and {@code @ComponentScan}.
 */
@SpringBootApplication(scanBasePackages = "com.youtopin.vaadin")
public class AdminPanelApplication {

    /**
     * Launches the Admin Panel application.
     *
     * @param args command line arguments passed by the JVM
     */
    public static void main(String[] args) {
        SpringApplication.run(AdminPanelApplication.class, args);
    }
}
