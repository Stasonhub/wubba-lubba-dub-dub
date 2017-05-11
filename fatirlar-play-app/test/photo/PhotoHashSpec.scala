package photo

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import org.apache.commons.io.IOUtils
import org.specs2.Specification
import service.PhotoService

class PhotoHashSpec extends Specification {

  def is =
    s2"""
       Photo hash must:

        give small difference on similar images 1 ${similarImages("similar_11.jpg", "similar_12.jpg")}
        give small difference on similar images 2 ${similarImages("similar_21.jpg", "similar_22.jpg")}
        give small difference on similar images 3 ${similarImages("similar_31.jpg", "similar_32.jpg")}

        give big difference on different images 1 ${differentImages("different_11.jpg", "different_12.jpg")}
        give big difference on different images 2 ${differentImages("different_21.jpg", "different_22.jpg")}
        give big difference on different images 3 ${differentImages("different_31.jpg", "different_32.jpg")}

        should be stable on big amount of images $collision
      """

  val photoService = new PhotoService

  def similarImages(photo1: String, photo2: String) = {
    val similar1 = readPhoto(s"samples/similar/$photo1")
    val similar2 = readPhoto(s"samples/similar/$photo2")

    val similar1Hash = photoService.calculateHash(similar1)
    val similar2Hash = photoService.calculateHash(similar2)

    (photoService.isTheSame(similar1Hash, similar2Hash) must beEqualTo(true))
      .setMessage(s"Two hashes should be close enough \n ${similar1Hash.toBinaryString} \n ${similar2Hash.toBinaryString}")
  }

  def differentImages(photo1: String, photo2: String) = {
    val similar1 = readPhoto(s"samples/different/$photo1")
    val similar2 = readPhoto(s"samples/different/$photo2")

    val similar1Hash = photoService.calculateHash(similar1)
    val similar2Hash = photoService.calculateHash(similar2)

    (photoService.isTheSame(similar1Hash, similar2Hash) must beEqualTo(false))
      .setMessage(s"Two hashes should be different enough \n ${similar1Hash.toBinaryString} \n ${similar2Hash.toBinaryString}")
  }

  def collision = {
    val collisionsMap =
      Range(1, 1517)//1517)
        .map(n => s"samples/collision/$n.jpg")
        .map(path => (readPhoto(path), path))
        .map(photoWithPath => (photoService.calculateHash(photoWithPath._1), photoWithPath._2))
        .groupBy(_._1)
        .filter(p => p._2.size > 1)
    collisionsMap must beEmpty
  }

  def readPhoto(location: String) = {
    val imageFile = IOUtils.toByteArray(getClass.getResource(location))
    ImageIO.read(new ByteArrayInputStream(imageFile))
  }


}
