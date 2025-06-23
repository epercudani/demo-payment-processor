package com.demopayment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.CommandLineRunner;
import com.demopayment.external.UserApiServer;

@SpringBootApplication
@EntityScan("com.demopayment.model")
@EnableJpaRepositories("com.demopayment.repository")
public class Main {
    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /*
     * User API Server is mocked for development purposes
     */
    @Bean
    public CommandLineRunner startUserApiServer() {
        return args -> {
            new Thread(() -> {
                try {
                    UserApiServer.start();
                } catch (Exception e) {
                    System.err.println("Failed to start UserApiServer: " + e.getMessage());
                }
            }, "UserApiServer-Thread").start();
        };
    }
} 