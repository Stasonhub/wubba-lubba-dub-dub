package com.airent.service.provider;

import com.airent.config.MvcConfig;
import com.airent.model.Photo;
import com.airent.service.PhotoService;
import com.airent.service.provider.api.ParsedAdvert;
import com.airent.service.provider.connection.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

@Service
public class PhotoContentService {

    private static final int SIGN_HEIGHT = 40;

    private Logger logger = LoggerFactory.getLogger(PhotoContentService.class);

    private OkHttpClient okHttpClient;
    private PhotoService photoService;
    private String storagePath;
    private boolean testMode;

    @Autowired
    public PhotoContentService(OkHttpClient okHttpClient,
                               PhotoService photoService,
                               @Value("${external.storage.path}") String storagePath,
                               @Value("${external.storage.test.mode}") boolean testMode) {
        this.okHttpClient = okHttpClient;
        this.photoService = photoService;
        this.storagePath = storagePath;
        this.testMode = testMode;
    }

    public List<Photo> savePhotos(String type, ParsedAdvert parsedAdvert) throws IOException {
        List<Photo> photos = new ArrayList<>();
        String photosPath = String.valueOf(System.currentTimeMillis());
        for (int i = 0; i < parsedAdvert.getPhotos().size(); i++) {
            String imageUrl = parsedAdvert.getPhotos().get(i);
            photos.add(savePhoto(type, photosPath, i, imageUrl));
        }
        return photos;
    }

    private Photo savePhoto(String type, String photosPath, int index, String imageUrl) throws IOException {
        String path = storagePath + File.separator + type + File.separator + photosPath + File.separator + index + ".jpg";
        new File(path).getParentFile().mkdirs();

        byte[] image = loadImage(imageUrl);

        long hash = writeImageContent(imageUrl, path, image);

        Photo photo = new Photo();
        photo.setPath(MvcConfig.STORAGE_FILES_PREFIX + File.separator + type + File.separator + photosPath + File.separator + index + ".jpg");
        photo.setMain(index == 0);
        photo.setHash(hash);
        return photo;
    }

    private byte[] loadImage(String imageUrl) throws IOException {
        Request request = new Request.Builder()
                .url(imageUrl)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36")
                .build();
        return okHttpClient.get().newCall(request).execute()
                .body().bytes();
    }

    private long writeImageContent(String imageUrl, String path, byte[] image) throws IOException {
        BufferedImage sourceBufferedImage = readImage(image);
        if (sourceBufferedImage == null) {
            throw new IllegalStateException("Failed to get buffered image from input " + imageUrl + ". Image size (bytes): " + image.length);
        }

        BufferedImage bufferedImage = removeAvitoSign(sourceBufferedImage);
        try (OutputStream out = testMode ? new NullOutputStream() : new FileOutputStream(new java.io.File(path))) {
            ImageIO.write(bufferedImage, "jpeg", out);
        }
        return photoService.calculateHash(bufferedImage);
    }


    public BufferedImage readImage(byte[] image) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(image));
    }

    private BufferedImage removeAvitoSign(BufferedImage originalImage) {
        int height = originalImage.getHeight() - SIGN_HEIGHT;
        return originalImage.getSubimage(0, 0, originalImage.getWidth(), height);
    }

}