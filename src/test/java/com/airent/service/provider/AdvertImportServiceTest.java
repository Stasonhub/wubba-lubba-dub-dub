package com.airent.service.provider;

import com.airent.mapper.AdvertImportMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;


@RunWith(SpringRunner.class)
@SpringBootTest
public class AdvertImportServiceTest {

    @Autowired
    private AdvertImportService advertImportService;

    @Test
    public void runImport() throws Exception {
        advertImportService.runImport("AVT");
    }

}