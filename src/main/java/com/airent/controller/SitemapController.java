package com.airent.controller;

import com.airent.mapper.AdvertMapper;
import com.airent.model.Advert;
import com.redfin.sitemapgenerator.ChangeFreq;
import com.redfin.sitemapgenerator.WebSitemapGenerator;
import com.redfin.sitemapgenerator.WebSitemapUrl;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Controller
public class SitemapController {

    private Logger logger = LoggerFactory.getLogger(SitemapController.class);

    @Value("${domain.link}")
    private String domainLink;

    @Autowired
    private AdvertMapper advertMapper;

    private Lock lock = new ReentrantLock();

    @RequestMapping(method = RequestMethod.GET, path = "/sitemap.xml")
    public void getSitemap(HttpServletResponse response) throws IOException {
        lock.lock();
        try {
            File file = new File(System.getProperty("java.io.tmpdir") + File.separator + "/ftr");
            file.mkdirs();

            File sitemap = new File(file, "sitemap.xml");

            if (!sitemap.exists() || sitemap.lastModified() < (System.currentTimeMillis() - 3600000)) {
                WebSitemapGenerator wsg = new WebSitemapGenerator(domainLink, file);
                wsg.addUrl(new WebSitemapUrl.Options(domainLink)
                        .lastMod(new Date())
                        .priority(1.0)
                        .changeFreq(ChangeFreq.HOURLY)
                        .build());
                wsg.addUrl(new WebSitemapUrl.Options(domainLink + "/search")
                        .lastMod(new Date())
                        .priority(1.0)
                        .changeFreq(ChangeFreq.HOURLY)
                        .build());

                // fill sitemap by batches
                long timestamp = System.currentTimeMillis();
                while (true) {
                    List<Advert> adverts = advertMapper.getNextAdvertsBeforeTime(timestamp, 100);
                    for (Advert advert : adverts) {
                        wsg.addUrl(new WebSitemapUrl.Options(domainLink + "/advert/" + advert.getId())
                                .lastMod(new Date(advert.getPublicationDate()))
                                .priority(0.7)
                                .changeFreq(ChangeFreq.DAILY)
                                .build());
                    }
                    if (adverts.isEmpty()) {
                        break;
                    }
                    timestamp = adverts.get(adverts.size() - 1).getPublicationDate();
                }

                List<File> sitemapFiles = wsg.write();
                if (sitemapFiles.size() != 1) {
                    logger.warn("Failed to generate sitemap.xml. Files: {}", sitemapFiles);
                    return;
                }
                sitemap = sitemapFiles.get(0);
            }

            try (InputStream sitemapStream = FileUtils.openInputStream(sitemap)) {
                response.setContentType("application/xml");
                IOUtils.copy(sitemapStream, response.getOutputStream());
                response.flushBuffer();
            }
        } finally {
            lock.unlock();
        }
    }

}