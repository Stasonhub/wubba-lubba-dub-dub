package com.airent.service.provider.http;

import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.TorInitializationListener;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.net.InetSocketAddress;
import java.net.Proxy;

@Service
public class JSoupTorConnector implements AutoCloseable {

    private Logger logger = LoggerFactory.getLogger(JSoupTorConnector.class);

    private final int PROXY_PORT = 9150;
    private final int TIMEOUT = 20_000;
    private static final String USER_AGENT =
            "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36";

    private TorClient torClient;
    private Proxy proxy;

    @PostConstruct
    public void start() throws InterruptedException {
        proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", PROXY_PORT));

        torClient = new TorClient();
        torClient.addInitializationListener(new TorInitializationListener() {
            @Override
            public void initializationProgress(String message, int percent) {
                logger.info("Tor initialization {} | {} ", percent, message);
            }

            @Override
            public void initializationCompleted() {
                logger.info("Tor initialization completed");
            }
        });
        torClient.start();
        torClient.enableSocksListener(PROXY_PORT);

        torClient.waitUntilReady();
    }

    public Connection connect(String url) {
        return Jsoup.connect(url)
                .proxy(proxy)
                .userAgent(USER_AGENT)
                .timeout(TIMEOUT);
    }

    @Override
    public void close() throws Exception {
        torClient.stop();
    }
}