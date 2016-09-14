package com.airent.mapper;

import com.airent.model.Photo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.SelectKey;

public interface PhotoMapper {

    @Insert("INSERT INTO photo (advertId, path) VALUES (#{advertId}, #{path})")
    @SelectKey(statement = "call identity()", keyProperty = "id", before = false, resultType = Integer.class)
    void createPhoto(Photo photo);

    @Delete("DELETE FROM photo WHERE id=#{id}")
    void deletePhoto(Photo photo);

}
