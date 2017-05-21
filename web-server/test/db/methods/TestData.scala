package db.methods

import java.util.concurrent.atomic.{AtomicInteger, AtomicLong}

import model.{Advert, District, Photo, User}

object TestData {

  val uniqIndex = (Math.random() * 899999 + 100000).toLong
  val phoneCounter = new AtomicLong(uniqIndex * 10000)
  val advertCounter = new AtomicInteger()

  def defaultUser = User(0, phoneCounter.incrementAndGet(), "User_2", 500000, Option.empty, false)
  def defaultAdvert = Advert(0, System.currentTimeMillis(), District.AV, "Address", 5, 12, 2, 42, 18000, false, 0, "AASSSDD", 23.24, 25.26, 0, 0, "self_imported", advertCounter.incrementAndGet())
  def defaultPhoto(advertId: Int) = Photo(0, advertId, "default_path_" + System.currentTimeMillis(), false, System.currentTimeMillis())

}
