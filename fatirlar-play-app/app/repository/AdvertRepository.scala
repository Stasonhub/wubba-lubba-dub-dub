package repository

import javax.inject.Singleton

import cats.implicits._
import doobie.imports.Fragments.{andOpt, whereAndOpt, in}
import doobie.imports._
import doobie.postgres.pgtypes._
import model.ui.AdvertPrices
import model.{Advert, District}

@Singleton
class AdvertRepository {

  implicit val districtAtom = pgJavaEnum[District]("districts_enum")

  def createAdvert(advert: Advert): Update0 =
    sql"""
         INSERT INTO advert (publicationDate, district, address, floor, maxFloor, rooms,
                              sq, price, withPublicServices, conditions, description, bedrooms, beds, latitude, longitude)
                 VALUES (${advert.publicationDate}, ${advert.district}, ${advert.address}, ${advert.floor}, ${advert.maxFloor}, ${advert.rooms}, ${advert.sq},
                          ${advert.price}, ${advert.withPublicServices}, ${advert.conditions}, ${advert.description}, ${advert.bedrooms}, ${advert.beds}, ${advert.latitude}, ${advert.longitude})
       """.update

  def findById(id: Int): Query0[Advert] =
    sql"""
           SELECT *
           FROM advert
           WHERE id = $id
            """.query[Advert]

  def getNextAdvertsBeforeTime(timestamp: Long, limit: Long): Query0[Advert] =
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
            """.query[Advert]

  def getAdverts(districts: List[District], priceFrom: Int, priceTo: Int, rooms: List[Int], offset: Long, limit: Long): Query0[Advert] = {
    val districtsF = districts.toNel.map(cs => in(fr"district", cs))
    val roomsF = rooms.toNel.map(cs => in(fr"rooms", cs))
    (fr"""
         SELECT adv.*
         FROM advert adv
              LEFT JOIN advert_author aut ON adv.id = aut.advertid
              LEFT JOIN sys_user usr ON aut.userid = usr.id
         """ ++
      whereAndOpt(
         Some(fr"""
           usr.trustrate > 3000 AND TO_TIMESTAMP((adv.publicationdate + 1209600000) / 1000) > CURRENT_TIMESTAMP
            AND price >= $priceFrom
            AND $priceTo >= price
           """),
         districtsF,
         roomsF) ++
      fr"""ORDER BY publicationDate DESC
        LIMIT $limit
        OFFSET $offset
            """).query[Advert]
  }

  def getAdvertsCount(districts: List[District], priceFrom: Int, priceTo: Int, rooms: List[Int]): Query0[Long] = {
    val districtsF = districts.toNel.map(cs => in(fr"district", cs))
    val roomsF = rooms.toNel.map(cs => in(fr"rooms", cs))
    (fr"""
        SELECT COUNT(*)
        FROM advert adv
             LEFT JOIN advert_author aut ON adv.id = aut.advertid
             LEFT JOIN sys_user usr ON aut.userid = usr.id """ ++
      whereAndOpt(
        Some(fr"""
            usr.trustrate > 3000 AND TO_TIMESTAMP((adv.publicationdate + 1209600000) / 1000) > CURRENT_TIMESTAMP
                       AND price >= $priceFrom
                       AND $priceTo >= price"""),
        districtsF,
        roomsF)
      ).query[Long]
  }

  def getAdvertPrices: AdvertPrices = ???

  def deleteAdvert(id: Long): Unit = ???

  def bindToUser(advertId: Int, userId: Int): Update0 =
    sql"""
            INSERT INTO advert_author (advertId, userId)
               VALUES ($advertId, $userId)
           """.update

  def findBySqPriceCoords(sq: Int, price: Int, lat: Double, lon: Double): Query0[Advert] =
    sql"""
            SELECT *
                 FROM advert
                 WHERE sq=$sq AND price=$price AND latitude=$lat AND longitude=$lon
            """.query[Advert]

}
