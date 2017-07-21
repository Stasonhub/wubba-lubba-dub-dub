package db.query

import db.TestConnection
import doobie.specs2.imports._
import model.{Advert, District}
import org.specs2.mutable._
import repository.AdvertRepository

class AdvertRepositorySpec extends Specification with AnalysisSpec {

  def transactor = TestConnection.dbConnection.xa

  val advertRepository = new AdvertRepository

  check(advertRepository.createAdvert(defaultAdvert))
  check(advertRepository.findById(0))
  check(advertRepository.findByOriginId(0))
  check(advertRepository.getNextAdvertsBeforeTime(0, 0))
  check(advertRepository.getAdverts(List[District](), 0, 0, List[Int](), 0, 0))
  check(advertRepository.getAdvertsCount(List[District](), 0, 0, List[Int]()))
  check(advertRepository.getAdverts(List(District.MS), 0, 0, List(1), 0, 0))
  check(advertRepository.getAdvertsCount(List(District.AV), 0, 0, List(3)))
  check(advertRepository.getAdverts(List(District.MS, District.KR), 0, 0, List(1, 2, 3), 0, 0))
  check(advertRepository.getAdvertsCount(List(District.NS, District.VH), 0, 0, List(1, 2, 3)))
  check(advertRepository.bindToUser(0, 0))
  check(advertRepository.findBySqPriceCoords(0, 0, 0.0, 0.0))

  def defaultAdvert = Advert(0, 123, District.AV, "Address", 3, 5, 2, 42, 12000, false, 0, "desc", 23.24, 25.26, 0, 0, "sad", 0)
}