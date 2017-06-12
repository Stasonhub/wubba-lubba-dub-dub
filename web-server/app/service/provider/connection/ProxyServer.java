package service.provider.connection;

import config.ProxyConfig;
import net.lightbody.bmp.BrowserMobProxy;
import net.lightbody.bmp.BrowserMobProxyServer;
import net.lightbody.bmp.client.ClientUtil;
import net.lightbody.bmp.proxy.auth.AuthType;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Singleton
public class ProxyServer {

    private BrowserMobProxy browserMobProxy;

    private org.openqa.selenium.Proxy seleniumProxy;
    private Proxy proxy;

    private List<String> whitelistPatterns;

    @Inject
    public ProxyServer(ProxyConfig proxyConfig) {
        browserMobProxy = new BrowserMobProxyServer();
        browserMobProxy.setConnectTimeout(90, TimeUnit.SECONDS);
        browserMobProxy.setRequestTimeout(90, TimeUnit.SECONDS);
        browserMobProxy.setChainedProxy(new InetSocketAddress(proxyConfig.host(), proxyConfig.port()));
        browserMobProxy.chainedProxyAuthorization(proxyConfig.username(), proxyConfig.password(), AuthType.BASIC);
        browserMobProxy.setTrustAllServers(true);

        whitelistPatterns = new ArrayList<>();
        whitelistPatterns.add("^https?://[a-zA-z0-9.]*avito\\..*");
        whitelistPatterns.add("^https?://api\\.ipify\\.org.*");
        whitelistPatterns.add("^https?://kazan.totook.*");
        browserMobProxy.whitelistRequests(whitelistPatterns, 410);

        start();
    }

    public List<String> getWhitelistPatterns() {
        return whitelistPatterns;
    }

    private void start() {
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