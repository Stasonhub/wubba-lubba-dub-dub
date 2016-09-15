package com.airent.service;

import com.airent.mapper.AdvertMapper;
import com.airent.model.Advert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdvertService {

    private static final int ADVERTS_PER_REQUEST = 15;

    @Autowired
    private AdvertMapper advertMapper;

    public List<Advert> getAdvertsForMainPage() {
        return advertMapper.getNextAdvertsBeforeTime(System.currentTimeMillis(), ADVERTS_PER_REQUEST);
    }

    public List<Advert> getAdvertsForMainPageFrom(long timestamp) {
        return advertMapper.getNextAdvertsBeforeTime(timestamp, ADVERTS_PER_REQUEST);
    }

}
