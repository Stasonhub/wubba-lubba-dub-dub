package db

import config.DbConnectionConfig
import repository.DbConnection

import doobie.imports._
import cats._, cats.data._, cats.implicits._
import fs2.interop.cats._

object TestConnection {

  val dbConnection = new DbConnection(
    DbConnectionConfig(
      "org.postgresql.Driver",
      "jdbc:postgresql://localhost:4466/postgres",
      "postgres",
      "AQGnthVu73AjBfBF"))

  def cleanUpDb = {
    sql"""
          DELETE FROM photo;
          DELETE FROM advert_author;
          DELETE FROM advertDetail;
          DELETE FROM sys_user;
      """.update.run.transact(dbConnection.xa).unsafePerformIO
  }

}
