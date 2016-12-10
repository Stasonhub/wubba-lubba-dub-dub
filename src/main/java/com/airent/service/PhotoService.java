package com.airent.service;

import com.airent.mapper.PhotoMapper;
import com.airent.model.Advert;
import com.airent.model.Photo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PhotoService {

    @Autowired
    private PhotoMapper photoMapper;

    public List<Photo> getPhotos(Advert advert) {
        return photoMapper.getPhotos(advert.getId());
    }

    public Map<Long, Photo> getMainPhotos(List<Advert> adverts) {
        if (adverts.isEmpty()) {
            return Collections.emptyMap();
        }
        return photoMapper.getMainPhotos(adverts
                .stream()
                .map(Advert::getId)
                .collect(Collectors.toList()))
                .stream()
                .collect(Collectors.toMap(v -> v.getAdvertId(), v -> v));
    }

}
