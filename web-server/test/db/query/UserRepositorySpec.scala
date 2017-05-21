package db.query

import db.TestConnection
import doobie.specs2.imports._
import model.User
import org.specs2.mutable._
import repository.UserRepository

class UserRepositorySpec extends Specification with AnalysisSpec {

  def transactor = TestConnection.dbConnection.xa

  val userRepository = new UserRepository

  check(userRepository.createUser(User(0, 0, "me", 0, Option.empty, false)))
  check(userRepository.updateUser(User(0, 0, "me", 0, Option.empty, false)))
  check(userRepository.findByPhone(123))
  check(userRepository.getUserForAdvert(12))
  check(userRepository.findByStartingSixNumbers(12, 123456))
  check(userRepository.arrangeRate(1, 2, 3, 0.1))

}