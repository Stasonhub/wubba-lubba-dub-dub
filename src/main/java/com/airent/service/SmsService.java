package com.airent.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.stereotype.Service;

@Service
public class SmsService {

    private static final String SMS_URL = "https://api.plivo.com/v1/Account/%s/Message/";

    private OkHttpClient client = new OkHttpClient();
    private String plivoAuthId;

    public SmsService() {
        //this.plivoAuthId = plivoAuthId;
    }

    public void sendSms(long phoneNumber, String message) {
//        Request request = new Request.Builder()
//                .url(String.format(SMS_URL, plivoAuthId))
//                .build();
        System.out.println("Phone number " + phoneNumber + " message: " + message);
    }

}
