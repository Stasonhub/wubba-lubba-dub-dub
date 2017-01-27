package com.airent.service.provider.avito;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class AvitoAdvertsProviderTest {

    @Test
    public void getImageUrlTest() {
        AvitoAdvertsProvider avitoAdvertsProvider = new AvitoAdvertsProvider(null, null, 0);

        String imageUrl = avitoAdvertsProvider.getImageUrl("background-image: url(\"//58.img.avito.st/80x60/2498266258.jpg\");");
        assertEquals("58.img.avito.st/80x60/2498266258.jpg", imageUrl);


        String imageUrl2 = avitoAdvertsProvider.getImageUrl("background-image: url(https://53.img.avito.st/80x60/2834141253.jpg);");
        assertEquals("53.img.avito.st/80x60/2834141253.jpg", imageUrl2);
    }
}