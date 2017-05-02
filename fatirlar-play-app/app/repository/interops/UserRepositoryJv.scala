package repository.interops

import java.util
import javax.inject.Singleton

import model.User

@Singleton
class UserRepositoryJv {

  def createUser(user: User) :User = ???

  def updateUser(user: User) = ???

  def findByPhone(phone: Long): User = ???

  def getUserForAdvert(advertId: Long): User = ???


  /**
    * @param phoneStartingNumbers phone left 6 digits
    */
  def findByStartingSixNumbers(advertId: Long, phoneStartingNumbers: Long): util.List[User] = ???

  /**
    * Set rate for specified user, and set different rate for others (rate*othersRateDecrease)
    */
  def arrangeRate(advertId: Long, userId: Long, trustRate: Int, othersRateDecrease: Double) = ???

}
