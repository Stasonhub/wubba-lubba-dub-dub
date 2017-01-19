package com.airent.service.provider;

import com.airent.config.MvcConfig;
import com.airent.model.Photo;
import com.airent.service.PhotoService;
import com.airent.service.provider.api.ParsedAdvert;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
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

@Service
public class PhotoContentService {

    private static final int SIGN_HEIGHT = 40;

    private PhotoService photoService;
    private String storagePath;

    @Autowired
    public PhotoContentService(PhotoService photoService,
                               @Value("${external.storage.path}") String storagePath) {
        this.photoService = photoService;
        this.storagePath = storagePath;
    }

    public List<Photo> savePhotos(String type, ParsedAdvert parsedAdvert) throws IOException {
        List<Photo> photos = new ArrayList<>();
        for (int i = 0; i < parsedAdvert.getPhotos().size(); i++) {
            String imageUrl = parsedAdvert.getPhotos().get(i);
            photos.add(savePhoto(type, i, imageUrl));
        }
        return photos;
    }

    private Photo savePhoto(String type, int index, String imageUrl) throws IOException {
        long photosPathId = System.currentTimeMillis();
        String path = storagePath + File.separator + type + File.separator + photosPathId + File.separator + index + ".jpg";
        new File(path).getParentFile().mkdirs();

        byte[] image = loadImage(imageUrl);

        BufferedImage bufferedImage =
                removeAvitoSign(ImageIO.read(new ByteArrayInputStream(image)));
        try (FileOutputStream out = new FileOutputStream(new java.io.File(path))) {
            ImageIO.write(bufferedImage, "jpeg", out);
        }

        Photo photo = new Photo();
        photo.setPath(MvcConfig.STORAGE_FILES_PREFIX + File.separator + "t" + File.separator + photosPathId + File.separator + index + ".jpg");
        photo.setMain(index == 0);
        photo.setHash(photoService.calculateHash(bufferedImage));
        return photo;
    }

    private byte[] loadImage(String imageUrl) throws IOException {
        Connection.Response response = Jsoup.connect(imageUrl).ignoreContentType(true).execute();
        return response.bodyAsBytes();
    }

    private BufferedImage removeAvitoSign(BufferedImage originalImage) {
        int height = originalImage.getHeight() - SIGN_HEIGHT;
        return originalImage.getSubimage(0, 0, originalImage.getWidth(), height);
    }

}