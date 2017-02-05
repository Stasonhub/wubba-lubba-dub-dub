package com.airent.service.provider.proxy;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

@Service
public class ProxyServer {

    private Proxy proxy;
    private String address;
    private String authentication;

    public ProxyServer(@Value("${proxy.host}") String proxyHost,
                       @Value("${proxy.port}") int proxyPort,
                       @Value("${proxy.username}") String userName,
                       @Value("${proxy.password}") String password) {
        this.address = proxyHost + ":" + proxyPort;
        this.authentication = userName + ":" + password;
        Authenticator.setDefault(new Authenticator() {
            public PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(
                        userName, password.toCharArray()
                );
            }
        });
        this.proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
    }

    public Proxy getProxy() {
        return proxy;
    }

    public String getAddress() {
        return address;
    }

    public String getAuthentication() {
        return authentication;
    }
}