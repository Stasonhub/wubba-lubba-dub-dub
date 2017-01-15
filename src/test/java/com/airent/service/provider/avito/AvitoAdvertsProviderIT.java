package com.airent.service.provider.avito;

import com.airent.config.OyoSpringTest;
import com.airent.service.provider.ProviderTester;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@OyoSpringTest
public class AvitoAdvertsProviderIT {

    @Autowired
    private AvitoAdvertsProvider avitoAdvertsProvider;

    @Test
    public void getAdverts() throws Exception {
        ProviderTester providerTester = new ProviderTester(2);
        providerTester.testGetAdverts(avitoAdvertsProvider);
    }


}