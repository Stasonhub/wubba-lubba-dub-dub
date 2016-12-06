package com.airent.mapper;

import org.apache.ibatis.annotations.Param;

public interface AdvertImportMapper {

    void saveLastImportTime(@Param("typeName") String typeName, @Param("lastImportDate") long lastImporDate);

    long getLastImportTime(String typeName);

}