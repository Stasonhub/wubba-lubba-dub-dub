package com.airent.service.provider;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.testng.annotations.Test;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;

import static org.testng.Assert.assertNotNull;

public class PhotoContentServiceTest {

    private Logger logger = LoggerFactory.getLogger(PhotoContentServiceTest.class);

    @Test
    public void testJpegIO() throws IOException {
        Iterator<ImageReader> readers = ImageIO.getImageReadersByFormatName("JPEG");
        while (readers.hasNext()) {
            logger.info("reader: {}", readers.next());
        }

        ClassPathResource classPathResource = new ClassPathResource("test_image.jpg");
        byte[] imageBytes = IOUtils.toByteArray(classPathResource.getInputStream());

        PhotoContentService photoContentService = new PhotoContentService(null, null, null, true);

        BufferedImage bufferedImage = photoContentService.readImage(imageBytes);
        assertNotNull(bufferedImage);
    }

}