package com.airent.config;

import com.airent.mapper.AdvertMapper;
import com.airent.mapper.PhotoMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.Distinct;
import com.airent.model.Photo;
import com.airent.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class TestDataConfig {

    private static final String[] TEXTS = {
            "Сдается однокомнатная уютная квартира на длительный срок. Квартира готова к проживанию. Огороженная территория, вид из окон на лес. В квартире есть все необходимое для проживания. Коммунальные платежи включены в стоимость проживания.",
            "Сдаю меблированную квартиру в хорошем состоянии, порядочным людям на длительный срок. Спальный гарнитур, встроенные шкафы, телевизор, кухонный гарнитур, холодильник, кондиционеры.Квартплата оплачивается дополнительно. Собственник.",
            "Сдается новая квартира на длительный срок. Мебель-частично. Рядом школа, детский сад, остановка. Предпочтение-молодая пара, можно с детьми. Аренда 11000+ КУ",
            "Однокомнатная квартира чистая, светлая и уютная квартира. Общий метраж 35 кв.м. комната – 18 кв.м., кухня – 8 кв.м.,с/узел – совмещенный стены в покраске, За 12000 руб. в месяц + квартплата. Хорошая транспортная развязка.",
            "Сдам хорошим людям уютную квартирку с хорошим ремонтом)) просьба, риелторам не беспокоить"
    };

    @Autowired
    private AdvertMapper advertMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PhotoMapper photoMapper;

    private long timestamp = System.currentTimeMillis();

    @PostConstruct
    public void init() {
        for (int i = 0; i < 47; i++) {
            User user = createTestUser(i);
            userMapper.createUser(user);

            for (int j = 0; j < 3; j++) {
                Advert testAdvert = createTestAdvert(i, j);
                testAdvert.setUserId(user.getId());
                advertMapper.createAdvert(testAdvert);

                List<Photo> photos = createPhotos(testAdvert);
                photos.forEach(v -> photoMapper.createPhoto(v));
            }
        }
    }

    private User createTestUser(int i) {
        User user = new User();
        user.setName("User " + i);
        user.setPhone(927400000 + i);
        return user;
    }

    private Advert createTestAdvert(int i, int j) {
        Advert advert = new Advert();
        advert.setPublicationDate(timestamp + (i * j) * 3600_000);
        advert.setConditions(2);
        advert.setDistrict(Distinct.VH);
        advert.setAddress(getStreetText(i * j / 15) + (i * j) % 12 + 3);
        advert.setFloor((i * j) % 5 + 3);
        advert.setMaxFloor((i * j) % 10 + 3);
        advert.setRooms(i * j % 3 + 1);
        advert.setSq((i * j) % 32 + 3);
        advert.setWithPublicServices(i * j % 2 == 0);
        advert.setPrice(((i * j) % 20 + 3) * 1000);
        advert.setDescription(TEXTS[i * j % 5]);
        advert.setMainPhotoUrl("images/test/test_image_" + ((i * j) % 3 + 1) + ".jpg");
        return advert;
    }

    private List<Photo> createPhotos(Advert advert) {
        List<Photo> photos = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Photo photo = new Photo();
            photo.setAdvertId(advert.getId());
            photo.setPath("images/test/test_image_" + i + ".jpg");
            photos.add(photo);
        }
        return photos;
    }

    private String getStreetText(int number) {
        switch (number) {
            case 0:
                return "ул. К.Маркса, ";
            case 1:
                return "ул. Х.Такташа, ";
            case 2:
                return "ул. Ленина, ";
            case 3:
                return "ул. Баумана, ";
            default:
                return "ул. А.Алиша, ";
        }
    }

}
