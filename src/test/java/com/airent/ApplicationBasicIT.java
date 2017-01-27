package com.airent;

import com.airent.config.OyoSpringTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@OyoSpringTest
public class ApplicationBasicIT extends AbstractTestNGSpringContextTests {

    @Test
    public void contextLoads() {
    }

}
