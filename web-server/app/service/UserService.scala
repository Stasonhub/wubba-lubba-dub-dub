package service

import javax.inject.Inject

import doobie.imports._
import fs2.interop.cats._
import model.User
import repository.{AdvertRepository, DbConnection, UserRepository}

class UserService @Inject() (dbConnection: DbConnection, advertRepository: AdvertRepository, userRepository: UserRepository) {

  def getUserForAdvert(advertId: Int): Option[User] =
    userRepository.getUserForAdvert(advertId)
      .option
      .transact(dbConnection.xa)
      .unsafePerformIO

}
