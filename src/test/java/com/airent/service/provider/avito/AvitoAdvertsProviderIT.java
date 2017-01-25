package com.airent.service.provider.avito;

import com.airent.config.OyoSpringTest;
import com.airent.mapper.AdvertMapper;
import com.airent.model.Advert;
import com.airent.service.provider.AdvertImportService;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@OyoSpringTest
public class AvitoAdvertsProviderIT {

    @Autowired
    private AdvertImportService advertImportService;

    @Value("${avito.provider.max.items}")
    private int avitoProviderMaxItems;

    @Autowired
    private AdvertMapper advertMapper;

    @Test
    public void getAdverts() throws Exception {
        assertTrue(avitoProviderMaxItems == 2);

        advertImportService.runImport("AVT");

        assertEquals(avitoProviderMaxItems, advertMapper.getCount());

        List<Advert> adverts = advertMapper.getNextAdvertsBeforeTime(0L, avitoProviderMaxItems);
        adverts.forEach(this::checkAdvert);
    }

    private void checkAdvert(Advert advert) {
        assertNotEquals(0, advert.getPrice());
        assertNotEquals(0, advert.getRooms());
        assertNotEquals(0, advert.getSq());
        assertNotEquals(0L, advert.getPublicationDate());
        assertFalse(StringUtils.isEmpty(advert.getAddress()));
        assertFalse(StringUtils.isEmpty(advert.getDescription()));
    }


}