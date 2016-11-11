package com.airent.security;

import org.springframework.stereotype.Service;

@Service
public class RecaptchaVerifier {

    private String secureKey;

    public boolean check(String remoteAddress, String responseKey) {
        return false;
    }

}
