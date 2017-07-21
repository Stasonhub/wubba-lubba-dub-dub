package service.provider

import java.awt.image.BufferedImage
import java.io.{ByteArrayInputStream, File, FileOutputStream}
import javax.imageio.ImageIO

import config.PhotosStorageConfig
import model.Photo
import okhttp3.Request
import org.slf4j.LoggerFactory
import service.ImagePHash
import service.provider.api.RawAdvert
import service.provider.connection.OkHttpClient


class PhotoPersistService(okHttpClient: OkHttpClient, photosStorageConfig: PhotosStorageConfig) {

  private val logger = LoggerFactory.getLogger(classOf[PhotoPersistService])
  private val signHeight = 40
  private val imagePHash = new ImagePHash()

  def savePhotos(providerType: String, rawAdvert: RawAdvert): List[Photo] = {
    val folder = photosStorageConfig.path + File.separator + providerType + File.separator + rawAdvert.originId

    if (new File(folder).exists)
      logger.error("Duplicated advert id on folder {}. Photos could be lost.", folder)
    else
      new File(folder).mkdirs

    rawAdvert.photos
      .zipWithIndex
      .map { case (url, index) => savePhoto(url, getPath(providerType, rawAdvert.originId, index), index == 0) }
  }

  private def getPath(providerType: String, originId: Long, index: Int) =
    photosStorageConfig.path + File.separator + providerType + File.separator + originId + File.separator + index + ".jpg"

  private def savePhoto(url: String, path: String, main: Boolean) = {
    val image = loadImage(url)
    val processedImage = processImage(image)

    try {
      val out = new FileOutputStream(new File(path))
      try
        ImageIO.write(processedImage, "jpeg", out)
      finally if (out != null) out.close()
    }

    Photo(0, 0, path, main, imagePHash.getHash(processedImage))
  }

  private def loadImage(imageUrl: String) = {
    val request = new Request.Builder().url(imageUrl).header("User-Agent", "Mozilla/5.0 (Windows NT 6.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2228.0 Safari/537.36").build
    okHttpClient.get.newCall(request).execute.body.bytes
  }

  private def processImage(image: Array[Byte]) = {
    val sourceBufferedImage = ImageIO.read(new ByteArrayInputStream(image))
    if (sourceBufferedImage == null) throw new IllegalStateException("Failed to get buffered image from input. Image size (bytes): " + image.length)
    removeAvitoSign(sourceBufferedImage)
  }

  private def removeAvitoSign(originalImage: BufferedImage) = {
    val height = originalImage.getHeight - signHeight
    originalImage.getSubimage(0, 0, originalImage.getWidth, height)
  }

}
