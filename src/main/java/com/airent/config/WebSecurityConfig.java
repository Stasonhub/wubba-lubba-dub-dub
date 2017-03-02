package com.airent.config;


import com.airent.security.*;
import com.airent.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.StandardPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    HttpAuthenticationEntryPoint authenticationEntryPoint;

    @Autowired
    AuthSuccessHandler authSuccessHandler;

    @Autowired
    AuthFailureHandler authFailureHandler;

    @Autowired
    HttpLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    CaptchaVerifierFilter captchaVerifierFilter;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth, PasswordEncoder passwordEncoder, LoginService loginService) throws Exception {
        auth.userDetailsService(loginService).passwordEncoder(passwordEncoder);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new StandardPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .and()
                .authorizeRequests()
                .antMatchers("/css/**", "/js/**", "/fonts/**", "/images/**")
                .permitAll()
                .antMatchers("/user/auth/**")
                .authenticated()
                .and()
                .formLogin()
                .permitAll()
                .loginProcessingUrl("/login")
                .loginPage("/login-page")
                .successHandler(authSuccessHandler)
                .usernameParameter("user")
                .passwordParameter("password")
                .successHandler(authSuccessHandler)
                .failureHandler(authFailureHandler)
                .and()
                .logout()
                .permitAll()
                .logoutRequestMatcher(new AntPathRequestMatcher("/login", "DELETE"))
                .logoutSuccessHandler(logoutSuccessHandler)
                .and()
                .addFilterBefore(captchaVerifierFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf()
                .disable();
    }
}
