package com.airent.mapper;

import com.airent.model.Advert;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

public interface AdvertMapper {

    @Insert("INSERT INTO advert (publicationDate, district, price, conditions, description) " +
            "VALUES (" +
            "#{publicationDate}, " +
            "#{district}, " +
            "#{price}, " +
            "#{conditions}, " +
            "#{description})")
    @SelectKey(statement = "call identity()", keyProperty = "id",
            before = false, resultType = Integer.class)
    void insertAdvert(Advert advert);


    @Select("SELECT * FROM advert")
    Advert findById(long id);

}
