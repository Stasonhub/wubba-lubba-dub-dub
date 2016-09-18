package com.airent.mapper;

import com.airent.model.Advert;
import com.airent.model.District;

import java.util.Collection;
import java.util.List;

public interface AdvertMapper {

    void createAdvert(Advert advert);

    Advert findById(long id);

    List<Advert> getNextAdvertsBeforeTime(@Param("timestamp") long timestamp, @Param("limit") int limit);

    //    @Select("SELECT * FROM advert WHERE " +
//            "district in " +
//            "<foreach item=\"item\" index=\"index\" collection=\"districts\" open=\"(\" separator=\",\" close=\")\">#{item}</foreach>" +
//            " AND " +
//            "price >= #{priceFrom} AND " +
//            "price <= #{priceTo} AND " +
//            //"rooms in (#{rooms}) AND " +
//            "publicationDate < #{timestamp} " +
//            "ORDER BY publicationDate DESC limit #{limit}")
    List<Advert> searchNextAdvertsBeforeTime(
            @Param("districts") Collection<District> districts,
            @Param("priceFrom") int priceFrom,
            @Param("priceTo") int priceTo,
            @Param("rooms") List<String> rooms,
            @Param("timestamp") long timestamp, @Param("limit") int limit);

    //@Delete("DELETE advert WHERE id=#{id}")
    void deleteAdvert(long id);

    // @Select("SELECT COUNT(*) FROM advert")
    int getCount();

}
