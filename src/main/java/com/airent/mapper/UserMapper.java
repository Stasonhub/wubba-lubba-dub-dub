package com.airent.mapper;

import com.airent.model.User;
import org.apache.ibatis.annotations.Insert;

public interface UserMapper {

    @Insert("INSERT INTO user (id, phone, name) VALUES (#{id}, #{phone}, #{name})")
    void createUser(User user);

}
