package com.airent;


import com.airent.mapper.AdvertMapper;
import com.airent.mapper.PhotoMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.District;
import com.airent.model.Photo;
import com.airent.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DbTests {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdvertMapper advertMapper;

    @Autowired
    private PhotoMapper photoMapper;

    private Advert createAdvert() {
        User user = new User();
        user.setName("Aidar");
        user.setPhone(12345);

        userMapper.createUser(user);

        Advert advert = new Advert();
        advert.setPublicationDate(2L);
        advert.setConditions(2);
        advert.setDescription("Advert");
        advert.setDistrict(District.KR);
        advert.setAddress("st. First,12");
        advert.setFloor(5);
        advert.setMaxFloor(10);
        advert.setSq(32);
        advert.setPrice(42_000);
        advert.setWithPublicServices(true);
        advert.setDescription("Bla bla bla");
        advert.setMainPhotoUrl("images/blblba.jpg");

        advert.setUserId(user.getId());

        advertMapper.createAdvert(advert);
        return advert;
    }

    @Test
    public void testCreateAdvertWithPhotos() {
        Advert advert = createAdvert();

        Advert selectedAdvert = advertMapper.findById(advert.getId());

        assertNotNull(selectedAdvert);
        assertEquals(advert.getPublicationDate(), selectedAdvert.getPublicationDate());
        assertEquals(advert.getConditions(), selectedAdvert.getConditions());
        assertEquals(advert.getDescription(), selectedAdvert.getDescription());
        assertEquals(advert.getDistrict(), selectedAdvert.getDistrict());
        assertEquals(advert.getAddress(), selectedAdvert.getAddress());
        assertEquals(advert.getPrice(), selectedAdvert.getPrice());

        Photo photo = new Photo();
        photo.setPath("/path/to/photo.jpg");
        photo.setAdvertId(advert.getId());
        photoMapper.createPhoto(photo);
    }

    @Test
    public void testSearchAdvert() {
        Advert advert = createAdvert();

        List<District> districtList = Collections.singletonList(advert.getDistrict());
        List<String> rooms = Collections.singletonList("1");
        List<Advert> adverts = advertMapper.searchNextAdvertsBeforeTime(districtList, 6_000, 45_000, rooms, System.currentTimeMillis(), 10);

        assertNotNull(adverts);
        assertEquals(1, adverts.size());
        assertEquals(advert.getId(), adverts.get(0).getId());
    }

}
