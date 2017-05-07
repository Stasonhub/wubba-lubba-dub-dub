package controllers

import javax.inject.{Inject, Singleton}

import model.District
import model.rest.{SearchParameters, SearchRequest}
import play.api.data.Form
import play.api.mvc.{Action, Controller}
import service.{AdvertService, PhotoService}

import play.api.data._
import play.api.data.Forms._

import scala.collection.JavaConverters._

@Singleton
class AdvertController @Inject()(advertService: AdvertService,
                                 photoService: PhotoService) extends Controller {


  def index = Action { request =>
    val adverts = advertService.advertsForMainPage.asScala.toList
    val mainPhotos = photoService.getMainPhotos(adverts.asJava).asScala.map { case (k, v) => (k.toInt, v) }.toMap
    Ok(views.html.index(adverts, mainPhotos))
  }

  def search = Action { request =>
    val searchForm = Form(
      mapping(
        "districts" -> list(of[District]),
        "priceRange" -> tuple(
          "priceFrom" -> number,
          "priceTo" -> number
        ),
        "rooms1" -> boolean,
        "rooms2" -> boolean,
        "rooms3" -> boolean,
        "page" -> optional(number)
      )(SearchParameters.apply)(SearchParameters.unapply)
    )

    val searchParameters = searchForm.get
    val adverts = advertService.adverts(searchParameters).asScala.toList
    val mainPhotos = photoService.getMainPhotos(adverts.asJava).asScala.map { case (k, v) => (k.toInt, v) }.toMap
    val pagesCount = advertService.pagesCount(searchParameters)

    Ok(views.html.search(adverts, mainPhotos, pagesCount, searchParameters))
  }

  def advert(advertId: Int) = Action { request =>
    val advert = advertService.advert(advertId)
    if (advert == null) {
      throw new IllegalArgumentException("Объявление не найдено")
    }

    val photos = photoService.getPhotos(advert).asScala.toList
    Ok(views.html.advert(advert, photos))
  }


}
