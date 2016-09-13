package com.airent.config;

import com.airent.mapper.AdvertMapper;
import com.airent.mapper.PhotoMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.Distinct;
import com.airent.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;

//@Configuration
public class TestDataConfig {

    @Autowired
    private AdvertMapper advertMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PhotoMapper photoMapper;

    @PostConstruct
    public void init() {
        for (int i = 0; i < 47; i++) {
            User user = createTestUser(i);
            userMapper.createUser(user);

            for (int j = 0; j < 3; j++) {
                Advert testAdvert = createTestAdvert(j);
                testAdvert.setUserId(user.getId());
                advertMapper.createAdvert(testAdvert);
            }
        }
    }

    private User createTestUser(int i) {
        User user = new User();
        user.setName("User " + i);
        user.setPhone(927400000 + i);
        return user;
    }

    private Advert createTestAdvert(int i) {
        Advert advert = new Advert();
        advert.setPublicationDate(System.currentTimeMillis());
        advert.setConditions(2);
        advert.setDistrict(Distinct.VH);
        advert.setAddress("ул. К.Маркса, 12");
        advert.setFloor(5);
        advert.setMaxFloor(10);
        advert.setSq(32);
        advert.setPrice(42);
        advert.setDescription("Сдается однокомнатная уютная квартира на длительный срок. Квартира готова к проживанию. Огороженная территория, вид из окон на лес. В квартире есть все необходимое для проживания. Коммунальные платежи включены в стоимость проживания.");
        advert.setMainPhotoUrl("images/test/test_image.jpg");
        return advert;
    }

    private void createTheSamePhotos(Advert advert) {

    }

}
