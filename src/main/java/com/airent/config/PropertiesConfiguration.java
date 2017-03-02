package com.airent.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

@Configuration
public class PropertiesConfiguration {

    @Configuration
    @Profile("prod")
    @PropertySource("classpath:app.prod.properties")
    static class Production {
    }

    @Configuration
    @Profile("dev")
    @PropertySource({"classpath:app.dev.properties"})
    static class Development {
    }


}