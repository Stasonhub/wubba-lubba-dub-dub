package repository

import javax.inject.{Inject, Singleton}

import config.DbConnectionConfig
import doobie.imports._
import cats._
import cats.data._
import cats.implicits._
import fs2.interop.cats._

@Singleton
class DbConnection @Inject() (dbConnectionConfig: DbConnectionConfig) {

  val xa = DriverManagerTransactor[IOLite](
    dbConnectionConfig.driver,
    dbConnectionConfig.url,
    dbConnectionConfig.username,
    dbConnectionConfig.password)

}
