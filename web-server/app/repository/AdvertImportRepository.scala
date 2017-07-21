package repository

import javax.inject.Singleton

import doobie.imports._

@Singleton
class AdvertImportRepository {

  def saveLastImportTime(typeName: String, lastImportDate: Long): Update0 =
    sql"""
         INSERT INTO importState
         VALUES ($typeName, $lastImportDate)
         ON CONFLICT (typeName) DO UPDATE
         SET lastImportDate = $lastImportDate""".update

  def lastImportTime(typeName: String): Query0[Long] =
    sql"""SELECT COALESCE(
                   (SELECT lastImportDate
                   FROM importState
                   WHERE typeName = $typeName)
                 ,0)""".query[Long]

}
