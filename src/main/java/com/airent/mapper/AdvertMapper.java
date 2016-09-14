package com.airent.mapper;

import com.airent.model.Advert;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.SelectKey;

import java.util.List;

public interface AdvertMapper {

    @Insert("INSERT INTO advert (userId, publicationDate, district, address, floor, maxFloor, rooms, sq, price, conditions, description, mainPhotoUrl) " +
            "VALUES (#{userId}, #{publicationDate}, #{district}, #{address}, #{floor}, #{maxFloor}, #{rooms}, #{sq}, #{price}, #{conditions}, #{description}, #{mainPhotoUrl})")
    @SelectKey(statement = "call identity()", keyProperty = "id", before = false, resultType = Integer.class)
    void createAdvert(Advert advert);

    @Select("SELECT * FROM advert WHERE id=#{id}")
    Advert findById(long id);

    @Select("SELECT * FROM advert WHERE publicationDate < #{timestamp} ORDER BY publicationDate DESC limit #{limit}")
    List<Advert> getNextAdvertsBeforeTime(long timestamp, int limit);

    @Delete("DELETE advert WHERE id=#{id}")
    void deleteAdvert(long id);

    @Select("SELECT COUNT(*) FROM advert")
    int getCount();

}
