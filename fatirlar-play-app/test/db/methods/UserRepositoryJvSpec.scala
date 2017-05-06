package db.methods

import db.TestConnection
import db.methods.TestData.{defaultAdvert, defaultUser}
import org.specs2.Specification
import org.specs2.specification.BeforeAll
import repository.interops.{AdvertRepositoryJv, UserRepositoryJv}
import repository.{AdvertRepository, UserRepository}

import scala.collection.JavaConverters._

class UserRepositoryJvSpec extends Specification with BeforeAll {

  val advertRepositoryJv = new AdvertRepositoryJv(TestConnection.dbConnection, new AdvertRepository())
  val userRepositoryJv = new UserRepositoryJv(TestConnection.dbConnection, new UserRepository())

  def is =
    s2"""
      The user repository should:
         create user $createUser
         update and find by phone user $updateUser
         find user bound to advert $findUserForAdvert
         find user by starting phone numbers $findUserByStartingNumbers
         arrange rate between users $arrangeRate
    """

  def createUser =
    userRepositoryJv.createUser(defaultUser) must not beNull

  def updateUser = {
    val originalUser = defaultUser
    val createdUser = userRepositoryJv.createUser(originalUser)
    userRepositoryJv.updateUser(createdUser.copy(name = "User_2_changed"))
    val foundUser = userRepositoryJv.findByPhone(createdUser.phone)

    foundUser must not(beNull)
    foundUser.name must beEqualTo("User_2_changed")
  }

  def findUserForAdvert = {
    val createdAdvert1 = advertRepositoryJv.createAdvert(defaultAdvert)
    val createdUser = userRepositoryJv.createUser(defaultUser)
    advertRepositoryJv.bindToUser(createdAdvert1.id, createdUser.id)

    val userForAdvert = userRepositoryJv.getUserForAdvert(createdAdvert1.id)
    userForAdvert must beEqualTo(createdUser)
  }

  def findUserByStartingNumbers = {
    val createdAdvert1 = advertRepositoryJv.createAdvert(defaultAdvert)
    val createdUser = userRepositoryJv.createUser(defaultUser)
    advertRepositoryJv.bindToUser(createdAdvert1.id, createdUser.id)

    val startindSixNumbers = (createdUser.phone / 10000).toInt
    val foundUser = userRepositoryJv.findByStartingSixNumbers(createdAdvert1.id, startindSixNumbers).asScala
    foundUser must containTheSameElementsAs(List(createdUser))
  }

  def arrangeRate = {
    val createdAdvert1 = advertRepositoryJv.createAdvert(defaultAdvert)

    val createdUser1 = userRepositoryJv.createUser(defaultUser.copy(trustRate = 100))
    val createdUser2 = userRepositoryJv.createUser(defaultUser.copy(trustRate = 100))
    val createdUser3 = userRepositoryJv.createUser(defaultUser.copy(trustRate = 100))

    advertRepositoryJv.bindToUser(createdAdvert1.id, createdUser1.id)
    advertRepositoryJv.bindToUser(createdAdvert1.id, createdUser2.id)
    advertRepositoryJv.bindToUser(createdAdvert1.id, createdUser3.id)

    userRepositoryJv.arrangeRate(createdAdvert1.id, createdUser1.id, 100000, 0.1)

    val userForAdvert = userRepositoryJv.getUserForAdvert(createdAdvert1.id)
    userForAdvert must be(createdUser1)

    val userByPhone2 = userRepositoryJv.findByPhone(createdUser2.phone)
    val userByPhone3 = userRepositoryJv.findByPhone(createdUser3.phone)

    userByPhone2.trustRate must beBetween(9L, 11L)
    userByPhone3.trustRate must beBetween(9L, 11L)
  }

  def beforeAll = TestConnection.cleanUpDb

}
