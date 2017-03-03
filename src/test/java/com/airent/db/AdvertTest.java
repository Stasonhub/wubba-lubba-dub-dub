package com.airent.db;

import com.airent.config.OyoSpringTest;
import com.airent.mapper.AdvertMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.District;
import com.airent.model.User;
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
    private AdvertMapper advertMapper;

    @Autowired
    private UserMapper userMapper;

    @Test
    public void testCreateAdvertWithPhotos() {
        Advert advert = TestUtil.createAdvert(advertMapper);
        User user = TestUtil.createUser(userMapper, 900_000_00_01L);
        advertMapper.bindToUser(advert.getId(), user.getId());

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
        Advert advert = TestUtil.createAdvert(advertMapper);
        User user = TestUtil.createUser(userMapper, 900_000_00_02L);
        advertMapper.bindToUser(advert.getId(), user.getId());

        List<District> districtList = Collections.singletonList(advert.getDistrict());
        List<Integer> rooms = Collections.singletonList(advert.getRooms());
        List<Advert> adverts = advertMapper.searchNextAdvertsBeforeTime(districtList, 6_000, 45_000, rooms, System.currentTimeMillis(), 10);

        assertNotNull(adverts);
        assertTrue(adverts.stream().map(Advert::getId).collect(Collectors.toList()).contains(advert.getId()));
    }

    @Test
    public void testFindBySqPriceCoords() {
        Advert advert = TestUtil.createAdvert(advertMapper);

        List<Advert> foundAdverts = advertMapper.findBySqPriceCoords(advert.getSq(), advert.getPrice(), advert.getLatitude(), advert.getLongitude());
        assertEquals(1, foundAdverts.size());
        assertEquals(advert.getId(), foundAdverts.get(0).getId());
    }

}
