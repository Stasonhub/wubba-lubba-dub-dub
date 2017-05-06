package db.methods

import db.TestConnection
import org.specs2.Specification
import org.specs2.specification.BeforeAll
import repository.AdvertImportRepository
import repository.interops.AdvertImportRepositoryJv

class AdvertImportRepositoryJvSpec extends Specification with BeforeAll {

  val advertImportRepositoryJv = new AdvertImportRepositoryJv(TestConnection.dbConnection, new AdvertImportRepository())

  def is =
    s2"""
      The advertDetail import repository should:
         save and get import time $saveAndGetImportTime
      """

  def saveAndGetImportTime = {
    advertImportRepositoryJv.saveLastImportTime("TTK", 123456L)
    advertImportRepositoryJv.saveLastImportTime("TTK", 4455L)
    advertImportRepositoryJv.saveLastImportTime("AVT", 45L)

    advertImportRepositoryJv.getLastImportTime("TTK") must beEqualTo(4455L)
    advertImportRepositoryJv.getLastImportTime("AVT") must beEqualTo(45L)
  }

  def beforeAll = TestConnection.cleanUpDb

}
