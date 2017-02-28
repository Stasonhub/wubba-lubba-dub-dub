package com.airent.complex;

import com.airent.config.OyoSpringTest;
import com.airent.service.provider.AdvertImportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@OyoSpringTest
@Test(groups = "complex")
public class AllTogetherComplexTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private AdvertImportService advertImportService;

    @Test//(timeOut = 90_000)
    public void getAdverts() throws Exception {
        advertImportService.runImport();
    }

}