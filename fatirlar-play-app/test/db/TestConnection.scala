package db

import config.DbConnectionConfig
import repository.DbConnection

object TestConnection {

  val dbConnection = new DbConnection(
    DbConnectionConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:4466/postgres",
      "postgres",
      "AQGnthVu73AjBfBF"))

}
