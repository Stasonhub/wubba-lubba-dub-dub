package com.airent.db;

import com.airent.mapper.AdvertMapper;
import com.airent.mapper.PhotoMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.Photo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@RunWith(SpringRunner.class)
@SpringBootTest
public class PhotoTest {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private AdvertMapper advertMapper;

    @Autowired
    private PhotoMapper photoMapper;

    @Test
    public void testPhotoCreate() {
        Advert advert = TestUtil.createAdvert(userMapper, advertMapper).getKey();

        Photo photo = new Photo();
        photo.setPath("/path/to/photo.jpg");
        photo.setAdvertId(advert.getId());
        photo.setHash(-1L);
        photoMapper.createPhoto(photo);
    }

    @Test
    public void testGetPhotoForAdvert() {
        Advert advert = TestUtil.createAdvert(userMapper, advertMapper).getKey();

        Photo photo = new Photo();
        photo.setPath("/path/to/photo.jpg");
        photo.setAdvertId(advert.getId());
        photo.setHash(-1L);
        photoMapper.createPhoto(photo);

        photo = new Photo();
        photo.setPath("main.jpg");
        photo.setAdvertId(advert.getId());
        photo.setMain(true);
        photo.setHash(-1L);
        photoMapper.createPhoto(photo);

        Photo mainPhoto = photoMapper.getMainPhoto(advert.getId());
        assertNotNull(mainPhoto);
        assertEquals("main.jpg", mainPhoto.getPath());

        List<Photo> photos = photoMapper.getPhotos(advert.getId());
        assertEquals(2, photos.size());
    }

    @Test
    public void testGetMainPhotosForAdverts() {
        List<Advert> advertList = new ArrayList();

        int advertsCount = 5;

        for (int i = 0; i < advertsCount; i++) {
            Advert advert = TestUtil.createAdvert(userMapper, advertMapper).getKey();
            advertList.add(advert);

            for (int j = 0; j < 2; j++) {
                Photo photo = new Photo();
                photo.setPath("/path/to/photo" + i + "" + j + ".jpg");
                photo.setAdvertId(advert.getId());
                photo.setMain(j == 0);
                photo.setHash(-1L);
                photoMapper.createPhoto(photo);
            }
        }

        List<Photo> mainPhotos = photoMapper.getMainPhotos(advertList.stream().map(v -> v.getId()).collect(Collectors.toList()));
        assertEquals(advertsCount, mainPhotos.size());
    }


}
