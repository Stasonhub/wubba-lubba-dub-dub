package com.airent.mapper;

import com.airent.model.Photo;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;

public interface PhotoMapper {

    @Insert("INSERT INTO photo (id, advertId, path) VALUES #{id}, #{advertId}, #{path}")
    void createPhoto(Photo photo);

    @Delete("DELETE FROM photo WHERE id=#{id}")
    void deletePhoto(Photo photo);

}
