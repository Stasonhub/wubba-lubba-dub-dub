package com.airent.mapper;

import com.airent.model.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {

    void createUser(User user);

    User findById(long id);

    User findByPhone(long phone);

    void updatePassword(@Param("id") long id, @Param("password") String password);

}
