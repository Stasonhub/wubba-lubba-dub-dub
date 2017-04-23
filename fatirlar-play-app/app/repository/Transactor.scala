package repository

import javax.inject.Singleton

import config.DbConnectionConfig
import doobie.imports._

@Singleton
class Transactor(dbConnectionConfig: DbConnectionConfig) {

  val xa = DriverManagerTransactor[IOLite](
    dbConnectionConfig.driver,
    dbConnectionConfig.url,
    dbConnectionConfig.username,
    dbConnectionConfig.password)

}
