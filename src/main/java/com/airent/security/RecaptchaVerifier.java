package com.airent.security;

import com.fasterxml.jackson.annotation.JsonProperty;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class RecaptchaVerifier {

    private String secureKey;
    private MappingJackson2HttpMessageConverter messageConverter;
    private OkHttpClient client;

    @Autowired
    public RecaptchaVerifier(MappingJackson2HttpMessageConverter messageConverter,
                             @Value("${google.captcha.secret}") String secretKey) {
        this.messageConverter = messageConverter;
        this.secureKey = secretKey;
        this.client = new OkHttpClient();
    }

    public boolean check(String remoteAddress, String responseKey) {
        RequestBody recaptchaParams = new FormBody.Builder()
                .add("secret", secureKey)
                .add("response", responseKey)
                .add("remoteip", remoteAddress)
                .build();

        Request request = new Request.Builder()
                .url("https://www.google.com/recaptcha/api/siteverify")
                .post(recaptchaParams)
                .build();

        try {
            Response response = client.newCall(request).execute();
            if (response.isSuccessful()) {
                String responseBody = response.body().string();
                RecaptchaResponse recaptchaResponse = messageConverter.getObjectMapper()
                        .readValue(responseBody, RecaptchaResponse.class);
                return recaptchaResponse.isSuccess();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static class RecaptchaResponse {

        private boolean success;
        @JsonProperty("challenge_ts")
        private String challengeTs;
        private String hostname;
        @JsonProperty("error-codes")
        private List<String> errorCodes;

        public RecaptchaResponse() {
        }

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getChallengeTs() {
            return challengeTs;
        }

        public void setChallengeTs(String challengeTs) {
            this.challengeTs = challengeTs;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }

        public List<String> getErrorCodes() {
            return errorCodes;
        }

        public void setErrorCodes(List<String> errorCodes) {
            this.errorCodes = errorCodes;
        }
    }

}
