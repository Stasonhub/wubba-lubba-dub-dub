package com.airent.service.provider.proxy;

import com.airent.config.OyoSpringTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.io.IOException;

@OyoSpringTest
public class ProxyServerTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private ProxyServer proxyServer;

    @Test
    public void proxyServerTest() throws IOException {
        proxyServer.checkAddress();
    }


}