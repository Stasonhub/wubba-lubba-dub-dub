package com.airent.db;

import com.airent.config.OyoSpringTest;
import com.airent.mapper.AdvertMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.District;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.*;

@OyoSpringTest
public class AdvertTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdvertMapper advertMapper;

    @Test
    public void testCreateAdvertWithPhotos() {
        Advert advert = TestUtil.createAdvert(userMapper, advertMapper).getKey();

        Advert selectedAdvert = advertMapper.findById(advert.getId());

        assertNotNull(selectedAdvert);
        assertEquals(advert.getPublicationDate(), selectedAdvert.getPublicationDate());
        assertEquals(advert.getConditions(), selectedAdvert.getConditions());
        assertEquals(advert.getDescription(), selectedAdvert.getDescription());
        assertEquals(advert.getDistrict(), selectedAdvert.getDistrict());
        assertEquals(advert.getAddress(), selectedAdvert.getAddress());
        assertEquals(advert.getPrice(), selectedAdvert.getPrice());
    }

    @Test
    public void testSearchAdvert() {
        Advert advert = TestUtil.createAdvert(userMapper, advertMapper).getKey();

        List<District> districtList = Collections.singletonList(advert.getDistrict());
        List<Integer> rooms = Collections.singletonList(advert.getRooms());
        List<Advert> adverts = advertMapper.searchNextAdvertsBeforeTime(districtList, 6_000, 45_000, rooms, System.currentTimeMillis(), 10);

        assertNotNull(adverts);
        assertTrue(adverts.stream().map(Advert::getId).collect(Collectors.toList()).contains(advert.getId()));
    }

}
