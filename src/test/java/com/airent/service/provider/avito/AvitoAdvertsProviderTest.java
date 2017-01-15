package com.airent.service.provider.avito;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AvitoAdvertsProviderTest {

    @Test
    public void getImageUrlTest() {
        AvitoAdvertsProvider avitoAdvertsProvider = new AvitoAdvertsProvider(null, null, 0);

        String imageUrl = avitoAdvertsProvider.getImageUrl("background-image: url(\"//58.img.avito.st/80x60/2498266258.jpg\");");
        assertEquals("58.img.avito.st/80x60/2498266258.jpg", imageUrl);
    }
}