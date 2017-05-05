package repository.interops

import java.util
import javax.inject.Singleton

import doobie.imports._
import fs2.interop.cats._
import model.ui.AdvertPrices
import model.{Advert, District}
import repository.{AdvertRepository, DbConnection}
import doobie.postgres.pgtypes._

import scala.collection.JavaConverters._

@Singleton
class AdvertRepositoryJv(dbConnection: DbConnection, advertRepository: AdvertRepository) {

  implicit val districtAtom = pgJavaEnum[District]("districts_enum")

  def createAdvert(advert: Advert): Advert =
    advertRepository.createAdvert(advert)
      .withUniqueGeneratedKeys[Advert]("id", "publicationdate", "district", "address", "floor", "maxfloor", "rooms", "sq", "price", "withpublicservices", "conditions", "description", "latitude", "longitude", "beds", "bedrooms")
      .transact(dbConnection.xa)
      .unsafePerformIO

  def findById(id: Int): Advert =
    advertRepository.findById(id)
      .unique
      .transact(dbConnection.xa)
      .unsafePerformIO

  def getNextAdvertsBeforeTime(timestamp: Long, limit: Int): util.List[Advert] =
    advertRepository.getNextAdvertsBeforeTime(timestamp, limit)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .asJava

  def getAdverts(districts: util.Collection[District],
                 priceFrom: Int,
                 priceTo: Int,
                 rooms: util.List[Integer],
                 offset: Int,
                 limit: Int): util.List[Advert] =
    advertRepository.getAdverts(districts.asScala.toList, priceFrom, priceTo, rooms.asScala.map(_.toInt).toList, offset, limit)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .asJava


  def getAdvertsCount(districts: util.Collection[District], priceFrom: Int, priceTo: Int, rooms: util.List[Integer]): Int =
    advertRepository.getAdvertsCount(districts.asScala.toList, priceFrom, priceTo, rooms.asScala.map(_.toInt).toList)
      .unique
      .transact(dbConnection.xa)
      .unsafePerformIO
      .toInt

  def getAdvertPrices: AdvertPrices = ???

  def deleteAdvert(id: Long): Unit = ???

  def bindToUser(advertId: Int, userId: Int): Unit =
    advertRepository.bindToUser(advertId, userId)
      .run
      .transact(dbConnection.xa)
      .unsafePerformIO

  def findBySqPriceCoords(sq: Int, price: Int, lat: Double, lon: Double): util.List[Advert] =
    advertRepository.findBySqPriceCoords(sq, price, lat, lon)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .asJava

}
