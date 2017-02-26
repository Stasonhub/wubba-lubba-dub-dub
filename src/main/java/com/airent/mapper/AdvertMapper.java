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

    List<Advert> searchNextAdvertsBeforeTime(
            @Param("districts") Collection<District> districts,
            @Param("priceFrom") int priceFrom,
            @Param("priceTo") int priceTo,
            @Param("rooms") List<Integer> rooms,
            @Param("timestamp") long timestamp,
            @Param("limit") int limit);

    AdvertPrices getAdvertPrices();

    void deleteAdvert(long id);

    int getCount();

    void bindToUser(@Param("advertId") long advertId, @Param("userId") long userId);

    Advert findBySqPriceCoords(int sq, int price, double lat, double lon);

}
