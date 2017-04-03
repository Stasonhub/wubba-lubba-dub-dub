package com.airent.mapper;

import com.airent.model.Advert;
import com.airent.model.District;
import com.airent.model.ui.AdvertPrices;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface AdvertMapper {

    void createAdvert(Advert advert);

    Advert findById(long id);

    List<Advert> getNextAdvertsBeforeTime(@Param("timestamp") long timestamp, @Param("limit") int limit);

    List<Advert> getAdverts(@Param("districts") Collection<District> districts,
                            @Param("priceFrom") int priceFrom,
                            @Param("priceTo") int priceTo,
                            @Param("rooms") List<Integer> rooms,
                            @Param("offset") int offset,
                            @Param("limit") int limit);

    int getAdvertsCount(@Param("districts") Collection<District> districts,
                        @Param("priceFrom") int priceFrom,
                        @Param("priceTo") int priceTo,
                        @Param("rooms") List<Integer> rooms);


    AdvertPrices getAdvertPrices();

    void deleteAdvert(long id);

    int getCount();

    void bindToUser(@Param("advertId") long advertId, @Param("userId") long userId);

    List<Advert> findBySqPriceCoords(@Param("sq") int sq, @Param("price") int price, @Param("lat") double lat, @Param("lon") double lon);

}
