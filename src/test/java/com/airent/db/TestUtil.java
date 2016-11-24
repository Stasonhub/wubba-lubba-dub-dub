package com.airent.db;

import com.airent.mapper.AdvertMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.District;
import com.airent.model.User;

public class TestUtil {

    public static Advert createAdvert(UserMapper userMapper, AdvertMapper advertMapper) {
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
        advert.setRooms(1);
        advert.setWithPublicServices(true);
        advert.setDescription("Bla bla bla");

        advertMapper.createAdvert(advert);
        return advert;
    }
}
