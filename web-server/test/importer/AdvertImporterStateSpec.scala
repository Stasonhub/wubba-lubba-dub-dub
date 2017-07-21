package importer

import db.TestConnection
import doobie.imports._
import fs2.interop.cats._
import importer.DataGenerator._
import org.specs2.Specification
import org.specs2.matcher.ThrownExpectations
import org.specs2.specification.BeforeAll
import repository.AdvertImportRepository

import scala.concurrent.duration._

class AdvertImporterStateSpec extends Specification with BeforeAll
  // sorry for thrown expectations mixin to acceptance specification
  with ThrownExpectations {

  def is =
    s2"""
      The advert importer should:
         should persist import times $shouldPersistImportTime
      """

  val advertImportRepository = new AdvertImportRepository

  def shouldPersistImportTime = {
    val avitoParser = mockAvitoParser(Map(
      1.hours.toMillis -> sampleAvitoAdvert(60, 1.hours.toMillis, 9274006001L, 100, (23.60, 24.60), List(photo1)),
      3.hours.toMillis -> sampleAvitoAdvert(61, 3.hours.toMillis, 9274006002L, 100, (23.60, 24.60), List(photo2))
    ))
    val totookParser = mockTotookParser(Map(
      1.hours.toMillis -> sampleTotookAdvert(62, 1.hours.toMillis, 927498L, 1000, (23.60, 24.60)),
      3.hours.toMillis -> sampleTotookAdvert(63, 3.hours.toMillis, 927499L, 1000, (23.60, 24.60))
    ))

    val importer = createImporter(avitoParser, totookParser)

    // run import process
    importer.runImportAvito(0L, 5.hours.toMillis)
    importer.runVerifyTotook(0L, 6.hours.toMillis)

    // check persist time
    advertImportRepository.lastImportTime(importer.avtType)
      .option
      .transact(TestConnection.dbConnection.xa)
      .unsafePerformIO must beSome.like { case lastImportTime =>
      lastImportTime must beEqualTo(5.hours.toMillis)
    }

    advertImportRepository.lastImportTime(importer.ttkType)
      .option
      .transact(TestConnection.dbConnection.xa)
      .unsafePerformIO must beSome.like { case lastImportTime =>
      lastImportTime must beEqualTo(6.hours.toMillis)
    }
  }

  override def beforeAll() = {
    TestConnection.cleanUpDb
  }
}
