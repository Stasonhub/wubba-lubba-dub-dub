package com.airent.security;

import com.airent.model.UserInfo;
import com.airent.service.LoginService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    private final ObjectMapper mapper;
    private final LoginService loginService;

    @Autowired
    AuthSuccessHandler(MappingJackson2HttpMessageConverter messageConverter, LoginService loginService) {
        this.mapper = messageConverter.getObjectMapper();
        this.loginService = loginService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setCharacterEncoding("UTF-8");

        UserInfo userInfo = new UserInfo();
        userInfo.setName(loginService.getUser(authentication).getName());
        mapper.writeValue(response.getWriter(), userInfo);
        response.getWriter().flush();
    }
}