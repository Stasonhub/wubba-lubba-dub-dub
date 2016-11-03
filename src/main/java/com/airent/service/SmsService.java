package com.airent.service;

import org.springframework.stereotype.Service;

@Service
public class SmsService {

    public void sendSms(long phoneNumber, String message) {
        System.out.println("Phone number " + phoneNumber + " message: " + message);
    }

}
