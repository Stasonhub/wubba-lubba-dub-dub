package service.provider;

import config.PhotosStorageConfig;
import model.Photo;
import service.PhotoService;
import service.provider.api.ParsedAdvert;
import service.provider.connection.OkHttpClient;
import okhttp3.Request;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.inject.Inject;
import javax.inject.Singleton;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Singleton
public class PhotoContentService {

    private static final int SIGN_HEIGHT = 40;

    private Logger logger = LoggerFactory.getLogger(PhotoContentService.class);

    private OkHttpClient okHttpClient;
    private PhotoService photoService;
    private PhotosStorageConfig photosStorageConfig;

    @Inject
    public PhotoContentService(OkHttpClient okHttpClient,
                               PhotoService photoService,
                               PhotosStorageConfig photosStorageConfig) {
        this.okHttpClient = okHttpClient;
        this.photoService = photoService;
        this.photosStorageConfig = photosStorageConfig;
    }

    public List<Photo> savePhotos(String type, ParsedAdvert parsedAdvert) throws IOException {
        return getPhotos(type, parsedAdvert, !photosStorageConfig.testMode());
    }

    private List<Photo> getPhotos(String type, ParsedAdvert parsedAdvert, boolean save) throws IOException {
        List<Photo> photos = new ArrayList<>();
        Set<Long> hashes = new HashSet<>();

        String folder = photosStorageConfig.path() + File.separator + type + File.separator + parsedAdvert.getOriginId();
        if (new File(folder).exists()) {
            logger.error("Duplicated advert id on folder {}. Photos could be lost.", folder);
        } else {
            new File(folder).mkdirs();
        }

        for (int i = 0; i < parsedAdvert.getPhotos().size(); i++) {
            String imageUrl = parsedAdvert.getPhotos().get(i);
            Photo photo = savePhoto(hashes, type, parsedAdvert.getOriginId(), i, imageUrl, save);
            if (photo != null) {
                photos.add(photo);
            }
        }
        return photos;
    }


    /**
     * @return could be null
     */
    private Photo savePhoto(Set<Long> hashes, String type, int originId,  int index, String imageUrl, boolean save) throws IOException {
        String path = photosStorageConfig.path() + File.separator + type + File.separator + originId + File.separator + index + ".jpg";

        byte[] image = loadImage(imageUrl);

        BufferedImage processedImage = processImage(image);

        long currentImageHash = photoService.calculateHash(processedImage);

        if (hashes.contains(currentImageHash)) {
            logger.warn("Ignoring image {} is duplicate in advert.", imageUrl);
            return null;
        }

        // save content
        try (OutputStream out = save ? new FileOutputStream(new java.io.File(path)) : new NullOutputStream()) {
            ImageIO.write(processedImage, "jpeg", out);
        }

        // create photo
        Photo photo = new Photo(
                0,
                0,
                "/photos"+ File.separator + type + File.separator + originId + File.separator + index + ".jpg",
                index == 0,
                currentImageHash
        );

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