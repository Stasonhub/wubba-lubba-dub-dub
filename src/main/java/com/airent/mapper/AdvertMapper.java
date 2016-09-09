package com.airent.mapper;

import com.airent.model.Advert;

public interface AdvertMapper {

    @Insert("insert into advert(name,email) values (#{name},#{email})")
    @SelectKey(statement = "call identity()", keyProperty = "id",
            before = false, resultType = Integer.class)
    void insertAdvert(Advert advert);
}
