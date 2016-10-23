package com.airent.service;

import com.airent.mapper.AdvertMapper;
import com.airent.mapper.UserMapper;
import com.airent.model.Advert;
import com.airent.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private AdvertMapper advertMapper;

    @Autowired
    private UserMapper userMapper;

    public User getUserForAdvert(long advertId) {
        Advert advert = advertMapper.findById(advertId);
        if (advert == null) {
            return null;
        }
        return userMapper.findById(advert.getUserId());
    }

}
