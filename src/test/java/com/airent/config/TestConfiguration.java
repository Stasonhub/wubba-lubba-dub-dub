package com.airent.config;

import org.springframework.context.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;

@Configuration
@Import(TestDbConfig.class)
@PropertySource("classpath:app.properties")
@ComponentScan(basePackages = "com.airent.service")
public class TestConfiguration {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new StandardPasswordEncoder();
    }
}