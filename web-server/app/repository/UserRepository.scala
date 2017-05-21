package repository

import javax.inject.Singleton

import doobie.imports._
import model.User

@Singleton
class UserRepository {

  def createUser(user: User): Update0 =
      sql"""
           INSERT INTO sys_user (phone, name, password, trustRate, registered)
           VALUES (${user.phone}, ${user.name}, ${user.password}, ${user.trustRate}, ${user.registered})
       """.update

  def updateUser(user: User): Update0 =
    sql"""
          UPDATE sys_user
          SET phone = ${user.phone}, name = ${user.name}, trustRate = ${user.trustRate}
          WHERE id = ${user.id}
      """.update

  def findByPhone(phone: Long): Query0[User] =
      sql"""
           SELECT *
           FROM sys_user
           WHERE phone=$phone
            """.query[User]

  def getUserForAdvert(advertId: Int): Query0[User] =
      sql"""
           SELECT *
           FROM sys_user
           WHERE id=(SELECT userId
                  FROM advert_author
                  WHERE advertId=$advertId
                  ORDER BY trustRate DESC
                  LIMIT 1)
            """.query[User]
  /**
    * @param phoneStartingNumbers phone left 6 digits
    */
  def findByStartingSixNumbers(advertId: Int, phoneStartingNumbers: Int): Query0[User] =
      sql"""
           SELECT *
           FROM sys_user
           WHERE id in (SELECT userId
                        FROM advert_author
                        WHERE advertId=$advertId) AND CAST(LEFT(phone::text,6) as INT)=$phoneStartingNumbers
            """.query[User]

  /**
    * Set rate for specified user, and set different rate for others (rate*othersRateDecrease)
    */
  def arrangeRate(advertId: Int, userId: Int, trustRate: Long, othersRateDecrease: Double): Update0 =
      sql"""
        UPDATE sys_user SET
         trustrate = CASE WHEN id=$userId THEN $trustRate ELSE (trustrate::DOUBLE PRECISION * $othersRateDecrease)::BIGINT END
        WHERE id in
                  (SELECT userId
                  FROM advert_author
                  WHERE advertId=$advertId)
        """.update

}
