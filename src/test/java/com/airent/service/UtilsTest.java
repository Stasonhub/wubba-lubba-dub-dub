package com.airent.service;

import com.airent.service.provider.common.Util;
import com.airent.template.Utils;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void testUtils() {
        assertEquals(4513324, Util.overwriteLast3Digit(4513857, 234324));
        assertEquals(3454634123L, Util.overwriteLast3Digit(3454634857L, 12123123123L));
        assertEquals(444444444002L, Util.overwriteLast3Digit(444444444444L, 2));
    }

}