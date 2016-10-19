package com.airent.mapper;

import com.airent.model.Photo;

import java.util.List;

public interface PhotoMapper {

    void createPhoto(Photo photo);

    void deletePhoto(Photo photo);

    Photo getMainPhoto(long advertId);

    List<Photo> getPhotos(long advertIds);

    List<Photo> getMainPhotos(List<Long> advertIds);

}
