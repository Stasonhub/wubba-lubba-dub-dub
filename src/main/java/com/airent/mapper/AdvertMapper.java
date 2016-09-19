package com.airent.mapper;

import com.airent.model.Advert;
import com.airent.model.District;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

public interface AdvertMapper {

    void createAdvert(Advert advert);

    Advert findById(long id);

    List<Advert> getNextAdvertsBeforeTime(@Param("timestamp") long timestamp, @Param("limit") int limit);

    List<Advert> searchNextAdvertsBeforeTime(
            Collection<District> districts,
            int priceFrom,
            int priceTo,
            List<String> rooms,
            long timestamp,
            int limit);

    void deleteAdvert(long id);

    int getCount();

}
