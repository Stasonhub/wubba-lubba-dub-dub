package com.airent.service.provider.connection;

import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.auth.AuthType;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Service
public class ProxyServer {

    private BrowserMobProxy browserMobProxy;

    private org.openqa.selenium.Proxy seleniumProxy;
    private Proxy proxy;

    public ProxyServer(@Value("${proxy.host}") String proxyHost,
                       @Value("${proxy.port}") int proxyPort,
                       @Value("${proxy.username}") String userName,
                       @Value("${proxy.password}") String password) {
        browserMobProxy = new BrowserMobProxyServer();
        browserMobProxy.setChainedProxy(new InetSocketAddress(proxyHost, proxyPort));
        browserMobProxy.chainedProxyAuthorization(userName, password, AuthType.BASIC);
    }

    @PostConstruct
    public void start() {
        browserMobProxy.start(0);
        seleniumProxy = ClientUtil.createSeleniumProxy(browserMobProxy);
        proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(browserMobProxy.getClientBindAddress(), browserMobProxy.getPort()));
    }

    public org.openqa.selenium.Proxy getSeleniumProxy() {
        return seleniumProxy;
    }

    public Proxy getProxy() {
        return proxy;
    }
}