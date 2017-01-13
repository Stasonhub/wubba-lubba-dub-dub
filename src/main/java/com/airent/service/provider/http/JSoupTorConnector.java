package com.airent.service.provider.http;

import com.subgraph.orchid.TorClient;
import com.subgraph.orchid.TorInitializationListener;
import org.jsoup.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;

@Service
public class JSoupTorConnector implements AutoCloseable {

    private Logger logger = LoggerFactory.getLogger(JSoupTorConnector.class);

    private JSoupConnector jSoupConnector;
    private TorClient torClient;
    private Proxy proxy;

    @Autowired
    public JSoupTorConnector(JSoupConnector jSoupConnector) {
        this.jSoupConnector = jSoupConnector;
    }

    @PostConstruct
    public void start() throws InterruptedException, IOException {
       // initTor();
    }

    private void initTor() throws IOException, InterruptedException {
        int port;

        try (ServerSocket s = new ServerSocket(0)) {
            port = s.getLocalPort();
        }

        proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", port));

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
        torClient.enableSocksListener(port);

        torClient.waitUntilReady();

        try {
            String text = connect("http://get-ip.me").get().select("h2").text();
            logger.info("Tor endpoint IP {}", text);
        } catch (Exception e) {
            logger.error("Failed to determine tor endpoint IP", e);
        }
    }

    public Connection connect(String url) {
        return jSoupConnector.connect(url)
                .proxy(proxy);
    }

    @Override
    public void close() throws Exception {
        torClient.stop();
    }


}