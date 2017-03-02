package com.airent;

import com.airent.config.PropertiesConfiguration;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;

@SpringBootApplication(exclude = {ThymeleafAutoConfiguration.class})
@MapperScan("com.airent.mapper")
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(new Object[]{Application.class, PropertiesConfiguration.class}, args);
    }
}
