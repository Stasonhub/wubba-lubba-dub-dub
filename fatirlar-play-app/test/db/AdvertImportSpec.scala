package db

import doobie.specs2.imports._
import doobie.util.iolite.IOLite
import org.specs2.mutable._
import repository.AdvertImportRepository
import doobie.imports._
import fs2.interop.cats._


class AdvertImportSpec extends Specification with AnalysisSpec {

  def transactor = DriverManagerTransactor[IOLite](
    "org.postgresql.Driver", "jdbc:postgresql://localhost:4466/postgres", "postgres", "AQGnthVu73AjBfBF"
  )

  val advertImportRepository = new AdvertImportRepository

  check(advertImportRepository.saveLastImportTime("something", 23))
  check(advertImportRepository.lastImportTime("something"))
}