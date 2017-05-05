package db.methods

import db.TestConnection
import model.{Advert, District, User}
import org.specs2.Specification
import repository.{AdvertRepository, PhotoRepository, UserRepository}
import repository.interops.{AdvertRepositoryJv, PhotoRepositoryJv, UserRepositoryJv}

import scala.collection.JavaConverters._

class AdvertsTest extends Specification {

  val advertRepositoryJv = new AdvertRepositoryJv(TestConnection.dbConnection, new AdvertRepository())
  val photoRepositoryJv = new PhotoRepositoryJv(TestConnection.dbConnection, new PhotoRepository())
  val userRepositoryJv = new UserRepositoryJv(TestConnection.dbConnection, new UserRepository())

  def is =
    s2"""
      The advert mapper should:
         create advert $createAdvert
         find created advert $findCreatedAdvert
         return created adverts by before time $findAdvertsBeforeTime
         search advert by parameters $searchAdverts
         count searching adverts $countAdverts
         find created advert by Sq/Price/Coord $findBySqPriceCoords
         bind advert to user $bindAdvertToUser
      """

  def createAdvert =
    advertRepositoryJv.createAdvert(defaultAdvert) must not beNull

  def findCreatedAdvert = {
    val originalAdvert = defaultAdvert
    val createdAdvert = advertRepositoryJv.createAdvert(originalAdvert)
    val foundAdvert = advertRepositoryJv.findById(createdAdvert.id)

    foundAdvert.publicationDate must beEqualTo(originalAdvert.publicationDate)
    foundAdvert.conditions must beEqualTo(originalAdvert.conditions)
    foundAdvert.description must beEqualTo(originalAdvert.description)
    foundAdvert.address must beEqualTo(originalAdvert.address)
    foundAdvert.price must beEqualTo(originalAdvert.price)
  }

  def findAdvertsBeforeTime = {
    val timestamp = System.currentTimeMillis() + 10000000

    val advertToCreate1 = defaultAdvert.copy(publicationDate = timestamp + 1)
    val advertToCreate2 = defaultAdvert.copy(publicationDate = timestamp + 2)
    val advertToCreate3 = defaultAdvert.copy(publicationDate = timestamp + 3)

    val createdAdvert1 = advertRepositoryJv.createAdvert(advertToCreate1)
    val createdAdvert2 = advertRepositoryJv.createAdvert(advertToCreate2)
    val createdAdvert3 = advertRepositoryJv.createAdvert(advertToCreate3)

    val createdUser = userRepositoryJv.createUser(defaultUser)
    advertRepositoryJv.bindToUser(createdAdvert1.id, createdUser.id)
    advertRepositoryJv.bindToUser(createdAdvert2.id, createdUser.id)
    advertRepositoryJv.bindToUser(createdAdvert3.id, createdUser.id)

    val adverts = advertRepositoryJv.getNextAdvertsBeforeTime(timestamp + 4, 2).asScala
    adverts should containTheSameElementsAs(List(createdAdvert3, createdAdvert2))
  }

  def searchAdverts = {
    val createdAdvert1 = advertRepositoryJv.createAdvert(defaultAdvert)
    val createdAdvert2 = advertRepositoryJv.createAdvert(defaultAdvert.copy(district = District.KR))

    val createdUser = userRepositoryJv.createUser(defaultUser)
    advertRepositoryJv.bindToUser(createdAdvert1.id, createdUser.id)
    advertRepositoryJv.bindToUser(createdAdvert2.id, createdUser.id)

    val foundAdverts = advertRepositoryJv.getAdverts(List(District.KR).asJava, 0, 15000, List(2).asJava.asInstanceOf[java.util.List[java.lang.Integer]], 0, 5).asScala
    foundAdverts should containTheSameElementsAs(List(createdAdvert2))
  }

  def countAdverts = {
    val createdAdvert1 = advertRepositoryJv.createAdvert(defaultAdvert)
    val createdAdvert2 = advertRepositoryJv.createAdvert(defaultAdvert.copy(district = District.MS))

    val createdUser = userRepositoryJv.createUser(defaultUser)
    advertRepositoryJv.bindToUser(createdAdvert1.id, createdUser.id)
    advertRepositoryJv.bindToUser(createdAdvert2.id, createdUser.id)

    val count = advertRepositoryJv.getAdvertsCount(List(District.MS).asJava, 0, 15000, List(2).asJava.asInstanceOf[java.util.List[java.lang.Integer]])
    count must beEqualTo(1)
  }

  def findBySqPriceCoords = {
    advertRepositoryJv.createAdvert(defaultAdvert)
    val createdAdvert1 = advertRepositoryJv.createAdvert(defaultAdvert.copy(sq = 1332, price = 12345, latitude = 1.1, longitude = 2.2))

    val foundAdverts = advertRepositoryJv.findBySqPriceCoords(1332, 12345, 1.1, 2.2).asScala
    foundAdverts should containTheSameElementsAs(List(createdAdvert1))
  }

  def bindAdvertToUser = {
    val createdAdvert = advertRepositoryJv.createAdvert(defaultAdvert)
    val createdUser = userRepositoryJv.createUser(defaultUser)
    advertRepositoryJv.bindToUser(createdAdvert.id, createdUser.id)
    createdAdvert must not beNull
  }

  def defaultAdvert = Advert(0, System.currentTimeMillis(), District.AV, "Address", 3, 5, 2, 42, 12000, false, 0, "desc", 23.24, 25.26, 0, 0)

  def defaultUser = User(0, 9274122334L, "User_1", 500000, Option.empty, false)

}
