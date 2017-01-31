package com.airent.service.provider.avito;

import com.airent.config.OyoSpringTest;
import com.airent.mapper.AdvertMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.User;
import com.airent.service.provider.AdvertImportService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.List;

import static com.airent.db.TestUtil.filterTestAdverts;
import static org.testng.Assert.*;

@OyoSpringTest
public class AvitoAdvertsProviderComplexTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AdvertImportService advertImportService;

    @Value("${avito.provider.max.items}")
    private int avitoProviderMaxItems;

    @Autowired
    private AdvertMapper advertMapper;

    @Autowired
    private UserMapper userMapper;

    @Test(timeOut = 180_000, enabled = false)
    public void getAdverts() throws Exception {
        assertTrue(avitoProviderMaxItems == 1);

        advertImportService.runImport("AVT");

        List<Advert> adverts = filterTestAdverts(advertMapper.getNextAdvertsBeforeTime(Long.MAX_VALUE, avitoProviderMaxItems));
        assertEquals(avitoProviderMaxItems, adverts.size());
        adverts.forEach(this::checkAdvert);
    }

    private void checkAdvert(Advert advert) {
        assertNotEquals(0, advert.getPrice());
        assertNotEquals(0, advert.getRooms());
        assertNotEquals(0, advert.getSq());
        assertNotEquals(0L, advert.getPublicationDate());
        assertFalse(StringUtils.isEmpty(advert.getAddress()));
        assertFalse(StringUtils.isEmpty(advert.getDescription()));

        User user = userMapper.getUserForAdvert(advert.getId());
        assertNotNull(user);

        assertFalse(StringUtils.isEmpty(user.getName()));
        assertNotEquals(0L, user.getPhone());
    }


}