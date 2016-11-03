package com.airent.service;

import com.airent.mapper.UserMapper;
import com.airent.model.User;
import com.airent.model.UserInfo;
import com.airent.util.PhoneNumber;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Objects;

@Service
public class LoginService implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private SmsService smsService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserInfo getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            UserInfo userLogin = new UserInfo();
            userLogin.setName(authentication.getName());
            return userLogin;
        }
        return null;
    }

    // do this transactional
    public boolean registerNewUser(long phoneNumber, String userName) {
        if (null != userMapper.findByPhone(phoneNumber)) {
            return false;
        }

        String password = generatePassword();

        User user = new User();
        user.setPhone(phoneNumber);
        user.setName(userName);
        user.setPassword(passwordEncoder.encode(password));
        userMapper.createUser(user);

        smsService.sendSms(phoneNumber, "Вы зарегистрированы на сайте airent.ru. Ваш пароль: " + password);
        return true;
    }

    public boolean sendNewPassword(long phoneNumber) {
        User user = userMapper.findByPhone(phoneNumber);
        if (null != user) {
            String password = generatePassword();
            String encodedPassword = passwordEncoder.encode(password);
            userMapper.updatePassword(user.getId(), encodedPassword);

            smsService.sendSms(user.getPhone(), "Ваш новый пароль на сайте airent.ru: " + password);
            return true;
        }
        return false;
    }

    private String generatePassword() {
        return RandomStringUtils.random(6, "abcdefghigklmnopqrstuvwxyzABCDEFGHIGKLMNOPQRSTUVWXYZ0123456789");
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            long phoneNumber = PhoneNumber.normalize(username);
            User user = userMapper.findByPhone(phoneNumber);
            Objects.requireNonNull(user);

            GrantedAuthority authority = new SimpleGrantedAuthority("BASIC");
            UserDetails userDetails = new org.springframework.security.core.userdetails.User(String.valueOf(user.getPhone()),
                    user.getPassword(), Arrays.asList(authority));
            return userDetails;
        } catch (Exception e) {
            throw new UsernameNotFoundException("Not found username " + username);
        }
    }
}
