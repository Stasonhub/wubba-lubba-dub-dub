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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return getPhotos(type, parsedAdvert, !testMode);
    }

    public List<Photo> getPhotosWithoutSave(String type, ParsedAdvert parsedAdvert) throws IOException {
        return getPhotos(type, parsedAdvert, false);
    }

    private List<Photo> getPhotos(String type, ParsedAdvert parsedAdvert, boolean save) throws IOException {
        List<Photo> photos = new ArrayList<>();
        String photosPath = String.valueOf(System.currentTimeMillis());
        Set<Long> hashes = new HashSet<>();
        for (int i = 0; i < parsedAdvert.getPhotos().size(); i++) {
            String imageUrl = parsedAdvert.getPhotos().get(i);
            Photo photo = savePhoto(hashes, type, photosPath, i, imageUrl,save);
            if (photo != null) {
                photos.add(photo);
            }
        }
        return photos;
    }

    /**
     * @return could be null
     */
    private Photo savePhoto(Set<Long> hashes, String type, String photosPath, int index, String imageUrl, boolean save) throws IOException {
        String path = storagePath + File.separator + type + File.separator + photosPath + File.separator + index + ".jpg";
        new File(path).getParentFile().mkdirs();

        byte[] image = loadImage(imageUrl);

        BufferedImage processedImage = processImage(image);

        long currentImageHash = photoService.calculateHash(processedImage);

        if (hashes.contains(currentImageHash)) {
            logger.warn("Ignoring image {} is duplicate in advert.", imageUrl);
            return null;
        }

        // save content
        try (OutputStream out = save ? new NullOutputStream() : new FileOutputStream(new java.io.File(path))) {
            ImageIO.write(processedImage, "jpeg", out);
        }

        // create photo
        Photo photo = new Photo();
        photo.setPath(MvcConfig.STORAGE_FILES_PREFIX + File.separator + type + File.separator + photosPath + File.separator + index + ".jpg");
        photo.setMain(index == 0);
        photo.setHash(currentImageHash);

        // add to global hashes list
        hashes.add(currentImageHash);
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

    private BufferedImage processImage(byte[] image) throws IOException {
        BufferedImage sourceBufferedImage = readImage(image);
        if (sourceBufferedImage == null) {
            throw new IllegalStateException("Failed to get buffered image from input. Image size (bytes): " + image.length);
        }
        return removeAvitoSign(sourceBufferedImage);
    }


    public BufferedImage readImage(byte[] image) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(image));
    }

    private BufferedImage removeAvitoSign(BufferedImage originalImage) {
        int height = originalImage.getHeight() - SIGN_HEIGHT;
        return originalImage.getSubimage(0, 0, originalImage.getWidth(), height);
    }

}