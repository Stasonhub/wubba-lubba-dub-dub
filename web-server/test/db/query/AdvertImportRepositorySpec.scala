package db.query

import db.TestConnection
import doobie.specs2.imports._
import org.specs2.mutable._
import repository.AdvertImportRepository

class AdvertImportRepositorySpec extends Specification with AnalysisSpec {

  def transactor = TestConnection.dbConnection.xa

  val advertImportRepository = new AdvertImportRepository

  check(advertImportRepository.saveLastImportTime("something", 23))
  check(advertImportRepository.lastImportTime("something"))
}