package com.airent.service.provider;

import com.airent.config.MvcConfig;
import com.airent.model.Photo;
import com.airent.service.PhotoService;
import com.airent.service.provider.api.ParsedAdvert;
import com.airent.service.provider.connection.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PhotoContentService {

    private static final int SIGN_HEIGHT = 40;

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

        long hash = writeImageContent(path, image);

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
                .addHeader("Content-Type", "application/json")
                .build();
        return okHttpClient.get().newCall(request).execute()
                .body().bytes();
    }

    private long writeImageContent(String path, byte[] image) throws IOException {
        if (testMode) {
            return UUID.randomUUID().getLeastSignificantBits();
        }

        BufferedImage bufferedImage =
                removeAvitoSign(ImageIO.read(new ByteArrayInputStream(image)));
        try (FileOutputStream out = new FileOutputStream(new java.io.File(path))) {
            ImageIO.write(bufferedImage, "jpeg", out);
        }
        return photoService.calculateHash(bufferedImage);
    }

    private BufferedImage removeAvitoSign(BufferedImage originalImage) {
        int height = originalImage.getHeight() - SIGN_HEIGHT;
        return originalImage.getSubimage(0, 0, originalImage.getWidth(), height);
    }

}