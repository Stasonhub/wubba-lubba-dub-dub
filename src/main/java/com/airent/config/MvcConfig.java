package com.airent.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.UrlResource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.resource.PathResourceResolver;

import java.net.MalformedURLException;

@Configuration
public class MvcConfig extends WebMvcConfigurerAdapter {

    public static final String STORAGE_FILES_PREFIX = "/strg";

    @Value("${external.storage.path}")
    private String externalStoragePath;

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            PathResourceResolver pathResourceResolver = new PathResourceResolver();
            pathResourceResolver.setAllowedLocations(new UrlResource("file:" + externalStoragePath));

            registry
                    .addResourceHandler(STORAGE_FILES_PREFIX + "/**")
                    .addResourceLocations("file:" + externalStoragePath + STORAGE_FILES_PREFIX)
                    .setCachePeriod(259200)
                    .resourceChain(true)
                    .addResolver(pathResourceResolver);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}