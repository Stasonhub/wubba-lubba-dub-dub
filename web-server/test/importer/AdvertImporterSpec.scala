package importer

import java.io.File

import config.PhotosStorageConfig
import db.TestConnection
import doobie.imports._
import fs2.interop.cats._
import importer.DataGenerator._
import model.Advert
import org.apache.commons.io.FileUtils
import org.specs2.Specification
import org.specs2.matcher.FileMatchers._
import org.specs2.matcher.ThrownExpectations
import org.specs2.mock.Mockito
import org.specs2.specification.BeforeAll
import repository.{AdvertImportRepository, AdvertRepository, PhotoRepository, UserRepository}
import service.PhotoService
import service.location.LocationServiceJv
import service.provider.api.{RawAdvert, VerifyAdvert}
import service.provider.avito.AvitoParser
import service.provider.connection.{OkHttpClient, ProxyServer}
import service.provider.totook.TotookParser
import service.provider.{AdvertImporter, PhotoPersistService}

import scala.concurrent.duration._

class AdvertImporterSpec extends Specification with BeforeAll with Mockito
  // sorry for thrown expectations mixin to acceptance specification
  with ThrownExpectations {

  def is =
    s2"""
      The advert importer should:
         save avt adverts $saveAvtAdverts
         overwrite trust rate with ttk adverts $overwriteWithTtkRate
         decrease trust rate for multiple adverts $decreaseRateForMultipleAdverts
         decrease trust rate for duplicated advert $decreaseRateForDuplicatesAdverts
         ignore "good" duplicates of the same advert from same user $ignoreFullDuplicates
      """


  val advertRepository = new AdvertRepository
  val photoRepository = new PhotoRepository
  val userRepository = new UserRepository
  val advertImportRepository = new AdvertImportRepository

  def saveAvtAdverts = {
    val avitoParser = mockAvitoParser(Map(
      1.hours.toMillis -> sampleAvitoAdvert(10, 1.hours.toMillis, 9274001123L, 100, (23.10, 24.10), List(photo1, photo2)),
      2.hours.toMillis -> sampleAvitoAdvert(11, 1.hours.toMillis, 9274001124L, 100, (23.11, 23.11), List(photo3)),
      3.hours.toMillis -> sampleAvitoAdvert(12, 3.hours.toMillis, 9274001125L, 100, (23.12, 23.12), List(photo4)),
      1.days.toMillis -> sampleAvitoAdvert(13, 1.days.toMillis, 9274001126L, 100, (23.13, 23.13), List(photo5)),
      2.days.toMillis -> sampleAvitoAdvert(14, 2.days.toMillis, 9274001127L, 100, (23.14, 23.14), List(photo6)),
      3.days.toMillis -> sampleAvitoAdvert(15, 3.days.toMillis, 9274001128L, 100, (23.15, 23.15), List(photo7))
    ))
    val totookParser = mockTotookParser(Map())
    val importer = createImporter(avitoParser, totookParser)

    // run import process
    importer.runImportAvito(0L, 2.days.toMillis)

    val importedAdverts = List(10, 11, 12, 13)
      .map(advertId => advertRepository.findByOriginId(advertId)
        .option
        .transact(TestConnection.dbConnection.xa)
        .unsafePerformIO
      )

    importedAdverts must contain((adv: Option[Advert]) => adv must beSome).forall

    importedAdverts
      .map(advertOpt => advertOpt.map(advert =>
        photoRepository.getPhotos(advert.id)
          .list
          .transact(TestConnection.dbConnection.xa)
          .unsafePerformIO
          .map(_.path)) must beSome.like { case x =>
        x must contain(allOf(beAnExistingPath))
      }
      )

    importedAdverts must contain((adv: Option[Advert]) => adv must beSome).forall

    // non imported adverts
    val nonImportedAdverts = List(14, 15)
      .map(advertId => advertRepository.findByOriginId(advertId)
        .option
        .transact(TestConnection.dbConnection.xa)
        .unsafePerformIO
      )

    nonImportedAdverts must contain((adv: Option[Advert]) => adv must beNone).forall
  }

  def overwriteWithTtkRate = {
    val avitoParser = mockAvitoParser(Map(
      1.hours.toMillis -> sampleAvitoAdvert(20, 1.hours.toMillis, 9274002123L, 100, (23.20, 24.20), List(photo8, photo9)),
      2.hours.toMillis -> sampleAvitoAdvert(21, 1.hours.toMillis, 9274112999L, 100, (23.20, 24.20), List(photo8, photo9))
    ))
    val totookParser = mockTotookParser(Map(
      1.hours.toMillis -> sampleTotookAdvert(23, 1.hours.toMillis, 927411L, 1000, (23.20, 24.20))
    ))

    val importer = createImporter(avitoParser, totookParser)

    // run import process
    importer.runImportAvito(0L, 3.hours.toMillis)
    importer.runVerifyTotook(0L, 3.hours.toMillis)

    // verify advert phone and rate was updated
    val advertIdOpt = advertRepository.findByOriginId(20)
      .option
      .transact(TestConnection.dbConnection.xa)
      .unsafePerformIO
      .map(_.id)

    // verify that user was bound
    val userOpt = advertIdOpt
      .flatMap(advertId => userRepository.getUserForAdvert(advertId)
        .option
        .transact(TestConnection.dbConnection.xa)
        .unsafePerformIO)

    userOpt must beSome.like { case user =>
      user.phone must beEqualTo(9274112999L)
      user.trustRate must beEqualTo(1000)
    }

    // verify that old user rate was decreased
    val oldUserOpt = userRepository.findByPhone(9274002123L)
      .option
      .transact(TestConnection.dbConnection.xa)
      .unsafePerformIO

    oldUserOpt must beSome.like { case user =>
      user.trustRate must beEqualTo(25)
    }
  }

  def decreaseRateForMultipleAdverts = {
    val avitoParser = mockAvitoParser(Map(
      1.hours.toMillis -> sampleAvitoAdvert(30, 1.hours.toMillis, 9274003001L, 100, (23.30, 24.30), List(photo11)),
      2.hours.toMillis -> sampleAvitoAdvert(31, 1.hours.toMillis, 9274003001L, 100, (23.31, 24.31), List(photo12)),
      3.hours.toMillis -> sampleAvitoAdvert(32, 1.hours.toMillis, 9274003001L, 100, (23.32, 24.32), List(photo13))
    ))
    val totookParser = mockTotookParser(Map())
    val importer = createImporter(avitoParser, totookParser)

    // run import process
    importer.runImportAvito(0L, 3.hours.toMillis)

    // verify that user rate was decreased
    val userOpt = userRepository.findByPhone(9274003001L)
      .option
      .transact(TestConnection.dbConnection.xa)
      .unsafePerformIO

    userOpt must beSome.like { case user =>
      user.trustRate must beEqualTo(25)
    }
  }

  def decreaseRateForDuplicatesAdverts = {
    val avitoParser = mockAvitoParser(Map(
      1.hours.toMillis -> sampleAvitoAdvert(40, 1.hours.toMillis, 9274004001L, 100, (23.40, 24.40), List(photo11, photo12)),
      2.hours.toMillis -> sampleAvitoAdvert(41, 1.hours.toMillis, 9274004002L, 100, (23.40, 24.40), List(photo11, photo12)),
      3.hours.toMillis -> sampleAvitoAdvert(42, 1.hours.toMillis, 9274004003L, 100, (23.40, 24.40), List(photo11, photo12))
    ))
    val totookParser = mockTotookParser(Map())
    val importer = createImporter(avitoParser, totookParser)

    // run import process
    importer.runImportAvito(0L, 4.hours.toMillis)

    val originUserOpt = userRepository.findByPhone(9274004001L)
      .option
      .transact(TestConnection.dbConnection.xa)
      .unsafePerformIO

    originUserOpt must beSome.like { case user =>
      user.trustRate must beEqualTo(100)
    }

    List(9274004002L, 9274004003L)
      .map(userPhone => userRepository.findByPhone(userPhone)
        .option
        .transact(TestConnection.dbConnection.xa)
        .unsafePerformIO)
      .map(followingUserOpt =>
        followingUserOpt must beSome.like { case followingUser =>
          followingUser.trustRate must beEqualTo(50)
        }
      )
  }

  def ignoreFullDuplicates = {
    val avitoParser = mockAvitoParser(Map(
      1.hours.toMillis -> sampleAvitoAdvert(50, 1.hours.toMillis, 9274005001L, 100, (23.50, 24.50), List(photo1, photo10, photo13)),
      2.hours.toMillis -> sampleAvitoAdvert(51, 1.hours.toMillis, 9274005001L, 100, (23.50, 24.50), List(photo1, photo10, photo13)),
      3.hours.toMillis -> sampleAvitoAdvert(52, 1.hours.toMillis, 9274005001L, 100, (23.50, 24.50), List(photo1, photo10, photo13))
    ))
    val totookParser = mockTotookParser(Map())
    val importer = createImporter(avitoParser, totookParser)

    // run import process
    importer.runImportAvito(0L, 4.hours.toMillis)

    // non imported adverts
    val nonImportedAdverts = List(41, 42)
      .map(advertId => advertRepository.findByOriginId(advertId)
        .option
        .transact(TestConnection.dbConnection.xa)
        .unsafePerformIO
      )
    nonImportedAdverts must contain((adv: Option[Advert]) => adv must beNone).forall
  }

  override def beforeAll() = {
    // cleanup data dir
    FileUtils.deleteQuietly(new File(photoStorePath))
    TestConnection.cleanUpDb
  }
}
