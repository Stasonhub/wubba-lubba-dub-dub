package repository

import java.util
import javax.inject.Singleton

import model.ui.AdvertPrices
import model.{Advert, District}


import javax.inject.Singleton

import doobie.imports._
import cats._
import cats.data._
import cats.implicits._
import config.DbConnectionConfig
import Fragments.{ in, andOpt }


import doobie.postgres.pgtypes._

@Singleton
class AdvertRepository {

  implicit val districtAtom = pgJavaEnum[District]("districts_enum")

  implicit def query0AsFragment[T](q: Query0[T]): Fragment = {
    q.asInstanceOf[Fragment]
  }

  def createAdvert(advert: Advert) = {
    val query: Fragment =
      sql"""
         INSERT INTO advert (publicationDate, district, address, floor, maxFloor, rooms,
                              sq, price, withPublicServices, conditions, description, bedrooms, beds, latitude, longitude)
                 VALUES (${advert.publicationDate}, ${advert.district}, ${advert.address}, ${advert.floor}, ${advert.maxFloor}, ${advert.rooms}, ${advert.sq},
                          ${advert.price}, ${advert.withPublicServices}, ${advert.conditions}, ${advert.description}, ${advert.bedrooms}, ${advert.beds}, ${advert.latitude}, ${advert.longitude})
       """
    query.update
     // .withUniqueGeneratedKeys[Advert]("id", "publicationDate", "district", "address", "floor", "maxFloor", "rooms", "sq", "price", "withPublicServices", "conditions", "description", "bedrooms", "beds", "latitude", "longitude")
  }

  def findById(id: Int) = {
    val query: Fragment =
      sql"""
           SELECT *
           FROM advert
           WHERE id = $id
            """
    query.query[Advert]
  }

  def getNextAdvertsBeforeTime(timestamp: Long, limit: Long) = {
    val query: Fragment =
      sql"""
           SELECT adv.*
           FROM advert adv
                LEFT JOIN advert_author aut ON adv.id = aut.advertid
                LEFT JOIN sys_user usr ON aut.userid = usr.id
           WHERE
             usr.trustrate > 3000 AND TO_TIMESTAMP((adv.publicationdate + 1209600000) / 1000) > CURRENT_TIMESTAMP
             AND $timestamp > publicationDate
           ORDER BY publicationDate DESC
           LIMIT $limit
            """
    query.query[Advert]
      //.process.list
  }

  def getAdverts(districts: List[District], priceFrom: Int, priceTo: Int, rooms: List[Int], offset: Long, limit: Long) = {
    val districtsF = districts.toNel.map(cs => in(fr"district", cs))
    val roomsF = rooms.toNel.map(cs => in(fr"rooms", cs))
    val query: Fragment =
      fr"""
         SELECT adv.*
         FROM advert adv
              LEFT JOIN advert_author aut ON adv.id = aut.advertid
              LEFT JOIN sys_user usr ON aut.userid = usr.id
         WHERE
          usr.trustrate > 3000 AND TO_TIMESTAMP((adv.publicationdate + 1209600000) / 1000) > CURRENT_TIMESTAMP""" ++
        andOpt(districtsF, roomsF) ++
      fr"""    AND price >= $priceFrom
          AND $priceTo >= price
        ORDER BY publicationDate DESC
        LIMIT $limit
        OFFSET $offset
            """
    query.query[Advert]
      //.process.list
  }

  def getAdvertsCount(districts: List[District], priceFrom: Int, priceTo: Int, rooms: List[Int]) = {
    val districtsF = districts.toNel.map(cs => in(fr"district", cs))
    val roomsF = rooms.toNel.map(cs => in(fr"rooms", cs))
    val query: Fragment =
      fr"""
        SELECT COUNT(*)
        FROM advert adv
             LEFT JOIN advert_author aut ON adv.id = aut.advertid
             LEFT JOIN sys_user usr ON aut.userid = usr.id
        WHERE
          usr.trustrate > 3000 AND TO_TIMESTAMP((adv.publicationdate + 1209600000) / 1000) > CURRENT_TIMESTAMP""" ++
        andOpt(districtsF, roomsF) ++
      fr"""AND price >= $priceFrom
          AND $priceTo >= price
            """
    query.query[Long]
  }

  def getAdvertPrices: AdvertPrices = ???

  def deleteAdvert(id: Long): Unit = ???

  def bindToUser(advertId: Long, userId: Long) = {
    val query: Fragment =
      sql"""
            INSERT INTO advert_author (advertId, userId)
               VALUES ($advertId, $userId)
           """
    query.update
  }

  def findBySqPriceCoords(sq: Int, price: Int, lat: Double, lon: Double) = {
    val query: Fragment =
      sql"""
            SELECT *
                 FROM advert
                 WHERE sq=$sq AND price=$price AND latitude=$lat AND longitude=$lon
            """
    query.query[Advert]
      //.process.list
  }

}
