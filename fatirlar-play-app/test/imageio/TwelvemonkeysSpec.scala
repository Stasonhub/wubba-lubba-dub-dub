package imageio

import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

import org.apache.commons.io.IOUtils
import org.specs2.Specification

class TwelvemonkeysSpec extends Specification {

  def is =
    s2"""
      ImageIO must (with twelvemonkeys inside):
        properly read jpg images $readImage
      """

  def readImage = {
    val imageFile = IOUtils.toByteArray(getClass.getResource("image.jpg"))
    val image = ImageIO.read(new ByteArrayInputStream(imageFile))
    image must not beNull
  }

}
