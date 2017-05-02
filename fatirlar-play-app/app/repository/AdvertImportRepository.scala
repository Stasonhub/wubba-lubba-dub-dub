package repository

import javax.inject.Singleton

import doobie.imports._
import cats._
import cats.data._
import cats.implicits._
import config.DbConnectionConfig

@Singleton
class AdvertImportRepository {

  implicit def query0AsFragment[T](q: Query0[T]): Fragment = {
    q.asInstanceOf[Fragment]
  }

  def saveLastImportTime(typeName: String, lastImportDate: Long) = {
    val query : Fragment = sql"""UPDATE importState SET lastImportDate = $lastImportDate WHERE typeName = $typeName"""
      query.update
  }

  def lastImportTime(typeName: String): Query0[Long] = {
    val query: Fragment = sql"""SELECT COALESCE(
                   (SELECT lastImportDate
                   FROM importState
                   WHERE typeName = $typeName)
                 ,0)"""
      query.query[Long]
  }

}
