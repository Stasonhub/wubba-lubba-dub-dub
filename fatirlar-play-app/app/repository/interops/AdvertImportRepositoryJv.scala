package repository.interops

import javax.inject.Singleton

import doobie.imports._
import cats._, cats.data._, cats.implicits._
import fs2.interop.cats._
import repository.{AdvertImportRepository, DbConnection}

@Singleton
class AdvertImportRepositoryJv(dbConnection: DbConnection, advertImportRepository: AdvertImportRepository) {

  def saveLastImportTime(typeName: String, lastImportDate: Long) = ???

  def getLastImportTime(typeName: String): Long = advertImportRepository.lastImportTime(typeName)
    .unique
    .transact(dbConnection.xa)
    .unsafePerformIO

}
