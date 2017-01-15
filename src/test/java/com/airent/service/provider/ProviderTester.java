package com.airent.service.provider;

import com.airent.service.provider.api.AdvertsProvider;
import com.airent.service.provider.api.ParsedAdvert;
import com.airent.service.provider.api.ParsedAdvertHeader;

import java.util.Iterator;

import static org.junit.Assert.*;

public class ProviderTester {

    private int advertsToTest;

    public ProviderTester(int advertsToTest) {
        this.advertsToTest = advertsToTest;
    }

    public void testGetAdverts(AdvertsProvider advertsProvider) throws Exception {
        Iterator<ParsedAdvertHeader> adverts = advertsProvider.getHeaders();
        for (int i = 0; i < advertsToTest; i++) {
            assertTrue(adverts.hasNext());
            ParsedAdvertHeader header = adverts.next();
            assertNotNull(header);

            checkAdvert(advertsProvider, header);
        }
    }

    private void checkAdvert(AdvertsProvider advertsProvider, ParsedAdvertHeader header) {
        assertNotEquals(0L, header.getPublicationTimestamp());
        assertNotNull(header.getAdvertUrl());

        ParsedAdvert advert = advertsProvider.getAdvert(header);
        assertNotNull(advert.getAddress());
        assertNotNull(advert.getPhone());
    }
}