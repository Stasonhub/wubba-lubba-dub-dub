package db

import doobie.specs2.imports._
import doobie.util.iolite.IOLite
import model.{Advert, District}
import org.specs2.mutable._
import doobie.imports._
import fs2.interop.cats._

import repository.AdvertRepository

class AdvertSpec extends Specification with AnalysisSpec {

  def transactor = DriverManagerTransactor[IOLite](
    "org.postgresql.Driver", "jdbc:postgresql://localhost:4466/postgres", "postgres", "AQGnthVu73AjBfBF"
  )

  val advertRepository = new AdvertRepository

  check(advertRepository.createAdvert(defaultAdvert))
  check(advertRepository.findById(0))
  check(advertRepository.getNextAdvertsBeforeTime(0, 0))
  check(advertRepository.getAdverts(List[District](), 0, 0, List[Int](), 0, 0))
  check(advertRepository.getAdvertsCount(List[District](), 0, 0, List[Int]()))
  check(advertRepository.bindToUser(0, 0))
  check(advertRepository.findBySqPriceCoords(0, 0, 0.0, 0.0))

  def defaultAdvert = Advert(0, 123, District.AV, "Address", 3, 5, 2, 42, 12000, false, 0, "desc", 23.24, 25.26, 0, 0)
}