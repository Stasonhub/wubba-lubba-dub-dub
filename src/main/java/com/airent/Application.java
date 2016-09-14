package com.airent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;

@SpringBootApplication(exclude = {ThymeleafAutoConfiguration.class})
@MapperScan("com.airent.mapper")
public class Application {

    //https://github.com/spring-projects/spring-boot/issues/4393
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
