package com.airent.service.provider.avito;

import com.airent.config.OyoSpringTest;
import com.airent.service.provider.api.ParsedAdvert;
import com.airent.service.provider.api.ParsedAdvertHeader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Iterator;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@OyoSpringTest
public class AvitoAdvertsProviderTest {

    private static final int ADVERTS_TO_TEST = 2;

    @Autowired
    private AvitoAdvertsProvider avitoAdvertsProvider;

    @Test
    public void getAdverts() throws Exception {
        Iterator<ParsedAdvertHeader> adverts = avitoAdvertsProvider.getHeaders();
        for (int i = 0; i < ADVERTS_TO_TEST; i++) {
            assertTrue(adverts.hasNext());
            ParsedAdvertHeader header = adverts.next();
            assertNotNull(header);

            checkAdvert(header);
        }
    }

    private void checkAdvert(ParsedAdvertHeader header) {
        assertNotEquals(0L, header.getPublicationTimestamp());
        assertNotNull(header.getAdvertUrl());

        ParsedAdvert advert = avitoAdvertsProvider.getAdvert(header);
        assertNotNull(advert.getAddress());
        assertNotNull(advert.getPhone());
    }

}