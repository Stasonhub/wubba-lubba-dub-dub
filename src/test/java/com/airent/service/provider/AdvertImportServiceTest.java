package com.airent.service.provider;

import com.airent.config.OyoSpringTest;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@Ignore
@RunWith(SpringRunner.class)
@OyoSpringTest
public class AdvertImportServiceTest {

    @Autowired
    private AdvertImportService advertImportService;

    @Test
    public void runImport() throws Exception {
        advertImportService.runImport();
    }

}