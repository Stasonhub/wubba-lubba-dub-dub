package com.airent.mapper;

public interface AdvertImporterMapper {

    void saveLastImportTime(String typeName, long time);

    long getLastImportTime(String typeName);

}