package com.airent.mapper;

import com.airent.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface UserMapper {

    void createUser(User user);

    void updateUser(User user);

    void updatePassword(@Param("id") long id, @Param("password") String password);

    User findById(long id);

    User findByPhone(long phone);

    User getUserForAdvert(long advertId);


    List<User> findByStartingFourNumbers(long phoneStartingNumbers);

    /**
     * Set rate for specified user, and set different rate for others (rate*othersRateDecrease)
     */
    void arrangeRate(long advertId, long userId, int rate, double othersRateDecrease);

}
