package com.airent.db;

import com.airent.mapper.AdvertMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.District;
import com.airent.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class TestUtil {

    public static final String TEST_ADVERT_DESC = "testAdvertDesc";

    public static List<Advert> filterTestAdverts(List<Advert> adverts) {
        return adverts.stream().filter(advert -> !TEST_ADVERT_DESC.equals(advert.getDescription())).collect(Collectors.toList());
    }

    public static Advert createAdvert(AdvertMapper advertMapper) {
        Advert advert = new Advert();
        advert.setPublicationDate(2L);
        advert.setConditions(2);
        advert.setDescription(TEST_ADVERT_DESC);
        advert.setDistrict(District.KR);
        advert.setAddress("st. First,12");
        advert.setFloor(5);
        advert.setMaxFloor(10);
        advert.setSq(32);
        advert.setPrice(42_000);
        advert.setRooms(1);
        advert.setLatitude(Math.random());
        advert.setLongitude(Math.random());
        advert.setWithPublicServices(true);
        advert.setDescription("Bla bla bla");
        advertMapper.createAdvert(advert);
        return advert;
    }

    public static User createUser(UserMapper userMapper, long phone) {
        User user = new User();
        user.setName("Aidar");
        user.setPhone(phone);
        user.setTrustRate(1_000);
        userMapper.createUser(user);
        return user;
    }

}
