package com.airent.service;

import com.airent.mapper.UserMapper;
import com.airent.model.User;
import com.airent.model.UserLogin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SmsService smsService;

    public UserLogin getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            UserLogin userLogin = new UserLogin();
            userLogin.setUsername(authentication.getName());
            return userLogin;
        }
        return null;
    }

    public boolean registerNewUser(long phoneNumber, String userName) {
        if (phoneNumber == 79274181281L) {
            // userMapper.ifUserFound(phoneNumber) return true
            return true;
        }

        String password = generatePassword();

        User user = new User();
        user.setPhone(phoneNumber);
        user.setName(userName);
        //user.setPassword(getSaltedPassword(password));
        userMapper.createUser(user);

        smsService.sendSms(phoneNumber, "Вы зарегистрированы на сайте airent.ru. Ваш пароль: " + password);

        return false;
    }

    public void sendNewPassword(long phoneNumber) {

    }

    private String generatePassword() {
        return "passwd";
    }

    private String getSaltedPassword(String password) {
        return "salted_passwd";
    }

}
