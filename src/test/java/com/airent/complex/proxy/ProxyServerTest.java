package com.airent.complex.proxy;

import com.airent.config.OyoSpringTest;
import com.airent.service.provider.connection.OkHttpClient;
import com.airent.service.provider.connection.ProxyServer;
import com.airent.service.provider.connection.WebDriver;
import okhttp3.Request;
import org.openqa.selenium.By;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;

@OyoSpringTest
@Test(groups = "complex")
public class ProxyServerTest extends AbstractTestNGSpringContextTests {

    private Logger logger = LoggerFactory.getLogger(ProxyServerTest.class);

    @Autowired
    private ProxyServer proxyServer;

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private WebDriver webDriver;

    private String checkerSite = "https://api.ipify.org?format=json";

    @Test
    public void proxyServerTest() throws IOException {
        String proxiedIpJson = okHttpClient.get().newCall(
                new Request.Builder()
                        .url(checkerSite)
                        .get()
                        .build()
        ).execute().body().string();
        compareToOriginalIp(proxiedIpJson);
    }

    @Test
    public void wedDriverProxyTest() throws IOException {
        String checkerSite = "https://api.ipify.org?format=json";
        webDriver.get().get(checkerSite);

        String ipString = webDriver.get().findElement(By.tagName("pre")).getText();
        compareToOriginalIp(ipString);
    }

    private void compareToOriginalIp(String ipString) throws IOException {
        String originalIpJson = new okhttp3.OkHttpClient.Builder().build().newCall(
                new Request.Builder()
                        .url(checkerSite)
                        .get()
                        .build()
        ).execute().body().string();

        logger.info("Original ip {}, proxied ip {}", originalIpJson, ipString);

        if (originalIpJson.equals(ipString)) {
            throw new IllegalArgumentException("Proxy method " + proxyServer.getProxy() + " is not working");
        }
    }

}