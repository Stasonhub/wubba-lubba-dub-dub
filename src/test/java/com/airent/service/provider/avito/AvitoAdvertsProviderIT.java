package com.airent.service.provider.avito;

import com.airent.config.OyoSpringTest;
import com.airent.service.provider.AdvertImportService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@OyoSpringTest
public class AvitoAdvertsProviderIT {

    @Autowired
    private AdvertImportService advertImportService;

    @Value("${avito.provider.max.items}")
    private int avitoProviderMaxItems;

    @Test
    public void getAdverts() throws Exception {
        assertTrue(avitoProviderMaxItems == 10);
        advertImportService.runImport("AVT");

    }


}