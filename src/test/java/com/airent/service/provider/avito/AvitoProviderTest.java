package com.airent.service.provider.avito;

import com.airent.model.Advert;
import com.airent.service.provider.api.RawAdvert;
import org.jsoup.Jsoup;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AvitoProviderTest {

    @Test
    public void testScanSomething() throws Exception {
        PhoneParser phoneParser = new PhoneParser();
        phoneParser.init();

        AvitoProvider avitoProvider = new AvitoProvider(phoneParser, 5, "/tmp/photos/1");
        List<RawAdvert> advertsUntil = avitoProvider.getAdvertsUntil(0L);

        advertsUntil.stream().map(RawAdvert::getAdvert).map(Advert::getDescription).forEach(System.out::println);

        phoneParser.close();
    }

    @Test
    public void testBackgroundMatcher() throws IOException {
        PhoneParser phoneParser = new PhoneParser();
        phoneParser.init();

        AvitoProvider avitoProvider = new AvitoProvider(phoneParser, 5, "/tmp/photos/2");
        String imageUrl = avitoProvider.getImageUrl("background-image: url(//68.img.avito.st/80x60/3186657868.jpg);");
        assertEquals("68.img.avito.st/80x60/3186657868.jpg", imageUrl);
    }

    @Test
    public void testJSoupImage() throws IOException {
        Jsoup.connect("http://www.freedigitalphotos.net/images/img/homepage/87357.jpg")
                .userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.100 Safari/537.36")
                .ignoreContentType(true).execute();
    }

}