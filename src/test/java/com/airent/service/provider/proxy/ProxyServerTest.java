package com.airent.service.provider.proxy;

import com.airent.config.OyoSpringTest;
import com.airent.service.provider.avito.AvitoAdvertsProvider;
import org.jsoup.Jsoup;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;

@OyoSpringTest
public class ProxyServerTest extends AbstractTestNGSpringContextTests {

    private Logger logger = LoggerFactory.getLogger(ProxyServerTest.class);

    @Autowired
    private ProxyServer proxyServer;

    @Autowired
    private AvitoAdvertsProvider avitoAdvertsProvider;

    private String checkerSite = "https://api.ipify.org?format=json";

    @Test
    public void proxyServerTest() throws IOException {
        String proxiedIpJson = Jsoup.connect(checkerSite)
                .ignoreContentType(true)
                .proxy(proxyServer.getProxy())
                .execute()
                .body();
        compareToOriginalIp(proxiedIpJson);
    }

    @Test
    public void wedDriverProxyTest() throws IOException {
        WebDriver driver = avitoAdvertsProvider.initDriver();

        String checkerSite = "https://api.ipify.org?format=json";
        driver.get(checkerSite);

        String ipString = driver.findElement(By.tagName("pre")).getText();
        compareToOriginalIp(ipString);
    }

    private void compareToOriginalIp(String ipString) throws IOException {
        String originalIpJson = Jsoup.connect(checkerSite)
                .ignoreContentType(true)
                .execute()
                .body();
        logger.info("Original ip {}, proxied ip {}", originalIpJson, ipString);

        if (originalIpJson.equals(ipString)) {
            throw new IllegalArgumentException("Proxy method " + proxyServer.getProxy().toString() + " is not working");
        }
    }

}