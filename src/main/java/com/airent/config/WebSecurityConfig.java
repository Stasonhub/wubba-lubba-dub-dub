package com.airent.config;


import com.airent.security.AuthFailureHandler;
import com.airent.security.AuthSuccessHandler;
import com.airent.security.HttpAuthenticationEntryPoint;
import com.airent.security.HttpLogoutSuccessHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    private HttpAuthenticationEntryPoint authenticationEntryPoint;
    @Autowired
    private AuthSuccessHandler authSuccessHandler;
    @Autowired
    private AuthFailureHandler authFailureHandler;
    @Autowired
    private HttpLogoutSuccessHandler logoutSuccessHandler;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("aidar").password("aidar").roles("AIDAR_ROLE");
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
                .antMatchers("/user/**")
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
                .csrf()
                .disable();
    }
}
