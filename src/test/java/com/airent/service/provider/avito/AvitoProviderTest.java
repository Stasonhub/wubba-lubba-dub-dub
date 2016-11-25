package com.airent.service.provider.avito;

import com.airent.model.Advert;
import com.airent.service.provider.api.RawAdvert;
import org.junit.Test;

import java.util.List;

public class AvitoProviderTest {

    @Test
    public void testScanSomething() throws Exception {
        PhoneParser phoneParser = new PhoneParser();
        phoneParser.init();

        AvitoProvider avitoProvider = new AvitoProvider(phoneParser);
        List<RawAdvert> advertsUntil = avitoProvider.getAdvertsUntil(0L);

        advertsUntil.stream().map(RawAdvert::getAdvert).map(Advert::getDescription).forEach(System.out::println);

        phoneParser.close();
    }

}