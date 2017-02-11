package com.airent.service.provider.connection;

import okhttp3.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class OkHttpClient {

    private okhttp3.OkHttpClient okHttpClient;

    @Autowired
    public OkHttpClient(ProxyServer proxyServer) {
        this.okHttpClient = new okhttp3.OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .proxy(proxyServer.getProxy())
                .proxyAuthenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Route route, Response response) throws IOException {
                        String credential = Credentials.basic(proxyServer.getUserName(), proxyServer.getPassword());
                        return response.request().newBuilder()
                                .header("Proxy-Authorization", credential)
                                .build();
                    }
                })
                .build();
    }

    public okhttp3.OkHttpClient get() {
        return okHttpClient;
    }
}