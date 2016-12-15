package com.airent.service;

import org.junit.Test;

import static org.junit.Assert.*;

public class ImagePHashTest {

    @Test
    public void distance() throws Exception {
        PhotoService.ImagePHash imagePHash = new PhotoService.ImagePHash();
        imagePHash.distance(123L, 2435L);
    }

}