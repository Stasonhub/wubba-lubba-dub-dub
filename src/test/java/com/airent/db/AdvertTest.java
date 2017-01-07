package com.airent.db;

import com.airent.config.OyoSpringTest;
import com.airent.mapper.AdvertMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.District;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(SpringRunner.class)
@OyoSpringTest
public class AdvertTest {

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
        assertEquals(1, adverts.size());
        assertEquals(advert.getId(), adverts.get(0).getId());
    }

}
