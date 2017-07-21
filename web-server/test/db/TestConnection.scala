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
      "jdbc:postgresql://localhost:5442/postgres",
      "postgres",
      ""))

  def cleanUpDb = {
    sql"""
          DELETE FROM importstate;
          DELETE FROM photo;
          DELETE FROM advert_author;
          DELETE FROM advert;
          DELETE FROM sys_user;
      """.update.run.transact(dbConnection.xa).unsafePerformIO
  }

}
