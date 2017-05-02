package repository

import javax.inject.Singleton

import config.DbConnectionConfig
import doobie.imports._
import cats._, cats.data._, cats.implicits._
import fs2.interop.cats._

@Singleton
class DbConnection(dbConnectionConfig: DbConnectionConfig) {

  val xa = DriverManagerTransactor[IOLite](
    dbConnectionConfig.driver,
    dbConnectionConfig.url,
    dbConnectionConfig.username,
    dbConnectionConfig.password)

}
