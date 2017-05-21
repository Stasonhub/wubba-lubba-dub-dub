package repository

import javax.inject.Singleton

import doobie.imports._

@Singleton
class AdvertImportRepository {

  def saveLastImportTime(typeName: String, lastImportDate: Long): Update0 =
    sql"""UPDATE importState SET lastImportDate = $lastImportDate WHERE typeName = $typeName""".update

  def lastImportTime(typeName: String): Query0[Long] =
    sql"""SELECT COALESCE(
                   (SELECT lastImportDate
                   FROM importState
                   WHERE typeName = $typeName)
                 ,0)""".query[Long]

}
