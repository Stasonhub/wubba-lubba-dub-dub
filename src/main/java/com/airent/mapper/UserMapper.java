package com.airent.mapper;

import com.airent.model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.SelectKey;

public interface UserMapper {

    @Insert("INSERT INTO user (phone, name) VALUES (#{phone}, #{name})")
    @SelectKey(statement = "call identity()", keyProperty = "id", before = false, resultType = Integer.class)
    void createUser(User user);

}
