package com.airent.security;

import com.airent.model.rest.AuthFailure;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class CaptchaVerifierFilter extends OncePerRequestFilter {

    @Autowired
    private RecaptchaVerifier recaptchaVerifier;

    @Autowired
    MappingJackson2HttpMessageConverter messageConverter;

    private List<RequestMatcher> requestMatchers;

    public CaptchaVerifierFilter() {
        requestMatchers = new ArrayList<>();
        requestMatchers.add(new AntPathRequestMatcher("/login", "POST"));
        requestMatchers.add(new AntPathRequestMatcher("/register", "POST"));
        requestMatchers.add(new AntPathRequestMatcher("/rememberPassword", "POST"));
    }

    @Override
    public void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                 FilterChain chain) throws IOException, ServletException {
        if (!requiresAuthentication(req)) {
            chain.doFilter(req, res);
            return;
        }

        String reCaptchaResponse = req.getParameter("recaptcha-token");
        if (reCaptchaResponse == null || !recaptchaVerifier.check(req.getRemoteAddr(), reCaptchaResponse)) {
            failure(res);
            return;
        }

        chain.doFilter(req, res);
    }

    protected boolean requiresAuthentication(HttpServletRequest request) {
        for (RequestMatcher requestMatcher : requestMatchers) {
            if (requestMatcher.matches(request)) {
                return true;
            }
        }
        return false;
    }

    private void failure(HttpServletResponse response) throws IOException {
        logger.debug("Invalid captcha value");

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        AuthFailure authFailure = new AuthFailure();
        authFailure.setMessage("Некорректное значение кода");
        messageConverter.getObjectMapper().writeValue(response.getWriter(), authFailure);
        response.getWriter().flush();
    }

}
