package com.airent.controller;

import com.airent.service.LoginService;
import com.airent.util.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

@Controller
public class LoginController {

    @Autowired
    private LoginService loginService;

    @RequestMapping(method = RequestMethod.GET, path = "/login")
    public void login(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/register")
    public void registerUser(HttpServletResponse response, String phoneNumber, String userName, @RequestParam("recaptcha-token") String captcha) {
        Objects.requireNonNull(phoneNumber);
        Objects.requireNonNull(userName);

        boolean created = loginService.registerNewUser(PhoneNumber.normalize(phoneNumber), userName);
        if (!created) {
            response.setStatus(HttpServletResponse.SC_CONFLICT);
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    @RequestMapping(method = RequestMethod.POST, path = "/rememberPassword")
    public void sendNewPassword(HttpServletResponse response, String phoneNumber, @RequestParam("recaptcha-token") String captcha) {
        Objects.requireNonNull(phoneNumber);

        boolean sent = loginService.sendNewPassword(PhoneNumber.normalize(phoneNumber));
        if (!sent) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
