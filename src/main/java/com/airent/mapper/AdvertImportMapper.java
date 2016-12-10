package com.airent.mapper;

import org.apache.ibatis.annotations.Param;

public interface AdvertImportMapper {

    void saveLastImportTime(@Param("typeName") String typeName, @Param("lastImportDate") long lastImportDate);

    long getLastImportTime(String typeName);

}