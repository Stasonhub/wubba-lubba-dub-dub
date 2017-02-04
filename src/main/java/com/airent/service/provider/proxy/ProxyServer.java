package com.airent.service.provider.proxy;

import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;

@Service
public class ProxyServer {

    private Logger logger = LoggerFactory.getLogger(ProxyServer.class);

    private Proxy proxy;
    private volatile boolean checked;

    public ProxyServer(@Value("${proxy.host}") String proxyHost,
                       @Value("${proxy.port}") int proxyPort,
                       @Value("${proxy.username}") String userName,
                       @Value("${proxy.password}") String password) {
        Authenticator.setDefault(
                new Authenticator() {
                    public PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(
                                userName, password.toCharArray()
                        );
                    }
                }
        );
        this.proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyHost, proxyPort));
    }

    public void checkAddress() {
        try {
            String checkerSite = "https://api.ipify.org?format=json";
            String originalIpJson = Jsoup.connect(checkerSite)
                    .ignoreContentType(true)
                    .execute()
                    .body();
            String proxiedIpJson = Jsoup.connect(checkerSite)
                    .ignoreContentType(true)
                    .proxy(proxy)
                    .execute()
                    .body();
            logger.info("Original ip {}, proxied ip {}", originalIpJson, proxiedIpJson);

            if (originalIpJson.equals(proxiedIpJson)) {
                throw new IllegalArgumentException("Proxy " + proxy.toString() + " is not working");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Proxy getProxy() {
        if (!checked) {
            synchronized (ProxyServer.class) {
                if (!checked) {
                    checkAddress();
                    checked = true;
                }
            }
        }
        return proxy;
    }


}