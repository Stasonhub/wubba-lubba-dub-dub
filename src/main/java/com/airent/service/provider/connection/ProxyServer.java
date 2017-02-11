package com.airent.service.provider.connection;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Service
public class ProxyServer {

    private String address;
    private String userName;
    private String password;

    private Proxy proxy;

    public ProxyServer(@Value("${proxy.host}") String proxyHost,
                       @Value("${proxy.port}") int proxyPort,
                       @Value("${proxy.username}") String userName,
                       @Value("${proxy.password}") String password) {
        this.address = proxyHost + ":" + proxyPort;
        this.userName = userName;
        this.password = password;
        this.proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
    }

    public Proxy getProxy() {
        return proxy;
    }

    public String getAddress() {
        return address;
    }

    public String getAuthentication() {
        return userName + ":" + password;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}