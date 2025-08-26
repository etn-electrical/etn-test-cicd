package com.example.github.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

    @Test
    void contextLoads() {
        // This test ensures that the Spring application context loads successfully
    }

    @Test
    void mainMethodTest() {
        // Test the main method can be called without throwing exceptions
        String[] args = {};
        Application.main(args);
    }
}
