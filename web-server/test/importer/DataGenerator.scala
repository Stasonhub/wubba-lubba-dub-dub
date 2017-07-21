package importer

import java.io.File

import config.PhotosStorageConfig
import db.TestConnection
import repository.{AdvertImportRepository, AdvertRepository, PhotoRepository, UserRepository}
import service.PhotoService
import service.location.LocationServiceJv
import service.provider.api.{RawAdvert, VerifyAdvert}
import service.provider.avito.AvitoParser
import service.provider.connection.{OkHttpClient, ProxyServer}
import service.provider.totook.TotookParser
import service.provider.{AdvertImporter, PhotoPersistService}

object DataGenerator {

  val photoStorePath = new File(System.getProperty("java.io.tmpdir")).getAbsolutePath + File.separator + "avertimporterspec"

  val photo1 = "http://www.stickees.com/files/animals/cat-meow/638-cat-food-hearts-sticker.png"
  val photo2 = "http://www.stickees.com/files/animals/cat-meow/642-cat-acrobat-sticker.png"
  val photo3 = "http://www.stickees.com/files/animals/cat-meow/644-cat-cart-sticker.png"
  val photo4 = "http://www.stickees.com/files/animals/cat-meow/651-cat-sing-sticker.png"
  val photo5 = "http://www.stickees.com/files/animals/cat-meow/648-cat-paper-sticker.png"
  val photo6 = "http://www.stickees.com/files/animals/cat-meow/637-cat-cupid-love-sticker.png"
  val photo7 = "http://www.stickees.com/files/animals/cat-meow/639-cat-fridge-sticker.png"
  val photo8 = "http://www.stickees.com/files/animals/cat-meow/640-cat-lady-sticker.png"
  val photo9 = "http://www.stickees.com/files/animals/cat-meow/641-cat-yarn-sticker.png"
  val photo10 = "http://www.stickees.com/files/animals/cat-meow/643-cat-birdhouse-sticker.png"
  val photo11 = "http://www.stickees.com/files/animals/cat-meow/645-cat-gift-sticker.png"
  val photo12 = "http://www.stickees.com/files/animals/cat-meow/647-cat-laptop-sticker.png"
  val photo13 = "http://www.stickees.com/files/animals/cat-meow/649-cat-pirate-sticker.png"
  val photo14 = "http://www.stickees.com/files/animals/cat-meow/650-cat-rascal-sticker.png"
  val photo15 = "http://www.stickees.com/files/animals/cat-meow/651-cat-sing-sticker.png"


  def sampleAvitoAdvert(
                         originId: Int,
                         publicationDate: Long,
                         phone: Long,
                         trustRate: Int,
                         coordinates: (Double, Double),
                         photos: List[String]) =
    RawAdvert(
      publicationDate,
      2,
      3,
      2,
      34,
      1,
      5,
      "ул.К.Маркса, 12",
      "Бла бла бла",
      coordinates._1,
      coordinates._2,
      23000,
      photos,
      "Mr.Smith",
      phone,
      trustRate,
      originId
    )

  def sampleTotookAdvert(
                          originId: Int,
                          publicationDate: Long,
                          phone: Long,
                          trustRate: Int,
                          coordinates: (Double, Double)) =
    VerifyAdvert(
      publicationDate,
      2,
      34,
      1,
      5,
      "ул.К.Маркса, 12",
      coordinates._1,
      coordinates._2,
      23000,
      phone,
      trustRate,
      originId
    )

  def mockAvitoParser(adverts: Map[Long, RawAdvert]): AvitoParser =
    new AvitoParser(null) {
      override def parseAdverts(fromTs: Long, untilTs: Long): List[RawAdvert] =
        adverts.filterKeys(ts => ts >= fromTs && ts < untilTs).values.toList
    }

  def mockTotookParser(adverts: Map[Long, VerifyAdvert]): TotookParser =
    new TotookParser() {
      override def parseAdverts(fromTs: Long, untilTs: Long): List[VerifyAdvert] =
        adverts.filterKeys(ts => ts >= fromTs && ts < untilTs).values.toList
    }


  def createImporter(avitoParser: AvitoParser,
                     totookParser: TotookParser) = {

    val advertImportRepository = new AdvertImportRepository
    val advertRepository = new AdvertRepository
    val userRepository = new UserRepository
    val photoRepository = new PhotoRepository
    val okHttpClient = new OkHttpClient(org.mockito.Mockito.mock(classOf[ProxyServer]))
    val photoPersistService = new PhotoPersistService(okHttpClient, PhotosStorageConfig(photoStorePath))
    val photoService = new PhotoService(TestConnection.dbConnection, photoRepository)
    val locationService = new LocationServiceJv()

    new AdvertImporter(
      TestConnection.dbConnection,
      avitoParser,
      totookParser,
      advertImportRepository,
      advertRepository,
      userRepository,
      photoRepository,
      photoPersistService,
      photoService,
      locationService
    )
  }


}
