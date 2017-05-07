package service

import java.util
import javax.inject.{Inject, Singleton}

import model.District
import model.rest.SearchParameters
import repository.interops.AdvertRepositoryJv

import scala.collection.JavaConverters._

@Singleton
class AdvertService @Inject()(advertRepositoryJv: AdvertRepositoryJv) {

  val advertsOnMainPage = 9
  val advertsPerRequest = 15

  def advertsForMainPage = advertRepositoryJv.getNextAdvertsBeforeTime(System.currentTimeMillis, advertsOnMainPage)

  def pagesCount(searchParameters: SearchParameters): Int = {
    val searchParameters = prepareSearchParameters(searchParameters)
    advertRepositoryJv.getAdvertsCount(searchParameters._1.asJavaCollection,
      searchParameters._3._1, searchParameters._3._2,
      searchParameters._2.map(v => v.asInstanceOf[Integer]).asJava) / advertsPerRequest
  }

  def adverts(searchParameters: SearchParameters) = {
    val searchParameters = prepareSearchParameters(searchParameters)
    advertRepositoryJv.getAdverts(
      searchParameters._1.asJavaCollection,
      searchParameters._3._1, searchParameters._3._2,
      searchParameters._2.map(v => v.asInstanceOf[Integer]).asJava,
      searchParameters._4.getOrElse(0) * advertsPerRequest,
      advertsPerRequest
    )
  }

  def advert(id: Int) = advertRepositoryJv.findById(id)

  private def prepareSearchParameters(searchParameters: SearchParameters) = (
    if (searchParameters.districts.isEmpty) util.EnumSet.allOf(classOf[District]).asScala.toList else searchParameters.districts,
    List(roomsVal(searchParameters.rooms1, 1), roomsVal(searchParameters.rooms2, 2), roomsVal(searchParameters.rooms3, 3)).flatten,
    (searchParameters.priceRange._1 * 1000, searchParameters.priceRange._2 * 1000),
    searchParameters.page
  )

  private def roomsVal(has: Boolean, room: Int): Option[Int] = if (has) Some(room) else None

}
