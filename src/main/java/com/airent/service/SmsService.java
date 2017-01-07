package com.airent.service;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;

@Service
public class SmsService {

    private Logger logger = LoggerFactory.getLogger(SmsService.class);

    private static final String SMS_URL = "https://gate.smsaero.ru/send/?user=%1s&password=%2s&to=7%3s&text=%4s&from=%5s&type=%6s";

    private String smsUser;
    private String smsPassword;
    private String smsFrom;
    private String smsType;

    private OkHttpClient client = new OkHttpClient();

    public SmsService(@Value("${sms.user}") String smsUser,
                      @Value("${sms.password}") String smsPassword,
                      @Value("${sms.from}") String smsFrom,
                      @Value("${sms.type}") String smsType) {
        this.smsUser = smsUser;
        this.smsPassword = smsPassword;
        this.smsFrom = smsFrom;
        this.smsType = smsType;
    }

    public void sendSms(long phoneNumber, String message) {
        logger.info("Sending message to: {} text: {}", phoneNumber, message);

        try {
            String messageUrl = String.format(SMS_URL,
                    smsUser,
                    smsPassword,
                    String.valueOf(phoneNumber),
                    URLEncoder.encode(message, "UTF-8"),
                    URLEncoder.encode(smsFrom, "UTF-8"),
                    smsType);

            Request request = new Request.Builder()
                    .url(messageUrl)
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new RuntimeException("Failed to send sms to " + phoneNumber + ". Response: " + response.body().string());
            }

            logger.info("Response from smsaero.ru {} ", response.body().string());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
