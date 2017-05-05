package repository.interops

import java.util
import javax.inject.Singleton

import doobie.imports._
import fs2.interop.cats._
import model.User
import repository.{DbConnection, UserRepository}

import scala.collection.JavaConverters._

@Singleton
class UserRepositoryJv(dbConnection: DbConnection, userRepository: UserRepository) {

  def createUser(user: User): User =
    userRepository.createUser(user)
      .withUniqueGeneratedKeys[User]("id", "phone", "name", "trustrate", "password", "registered")
      .transact(dbConnection.xa)
      .unsafePerformIO

  def updateUser(user: User): Unit =
    userRepository.updateUser(user)
      .run
      .transact(dbConnection.xa)
      .unsafePerformIO

  def findByPhone(phone: Long): User =
    userRepository.findByPhone(phone)
      .unique
      .transact(dbConnection.xa)
      .unsafePerformIO

  def getUserForAdvert(advertId: Int): User =
    userRepository.getUserForAdvert(advertId)
      .unique
      .transact(dbConnection.xa)
      .unsafePerformIO

  /**
    * @param phoneStartingNumbers phone left 6 digits
    */
  def findByStartingSixNumbers(advertId: Int, phoneStartingNumbers: Int): util.List[User] =
    userRepository.findByStartingSixNumbers(advertId, phoneStartingNumbers)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .asJava

  /**
    * Set rate for specified user, and set different rate for others (rate*othersRateDecrease)
    */
  def arrangeRate(advertId: Int, userId: Int, trustRate: Long, othersRateDecrease: Double): Unit =
    userRepository.arrangeRate(advertId, userId, trustRate, othersRateDecrease)
      .run
      .transact(dbConnection.xa)
      .unsafePerformIO

}
