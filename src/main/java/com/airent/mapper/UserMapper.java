package com.airent.mapper;

import com.airent.model.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    void createUser(User user);

    void updateUser(User user);

    void updatePassword(@Param("id") long id, @Param("password") String password);

    User findById(long id);

    User findByPhone(long phone);

    User getUserForAdvert(long advertId);
}
