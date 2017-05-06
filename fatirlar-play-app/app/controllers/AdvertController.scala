package controllers

import javax.inject.{Inject, Singleton}

import model.rest.SearchRequest
import play.api.mvc.Controller
import service.{AdvertService, PhotoService}

import scala.collection.JavaConverters._

@Singleton
class AdvertController @Inject()(advertService: AdvertService,
                                 photoService: PhotoService) extends Controller {


  def index = {
    val adverts = advertService.getAdvertsForMainPage.asScala
    val mainPhotos = photoService.getMainPhotos(adverts.asJava).asScala
    Ok(views.html.index(adverts, mainPhotos))
  }

  def search = {
    val searchRequest: SearchRequest = null
    val normalized = searchRequest.normalize()

    val adverts = advertService.getAdverts(normalized).asScala
    val mainPhotos = photoService.getMainPhotos(adverts.asJava).asScala.toMap
    val pagesCount = advertService.getPagesCount(normalized)

    Ok(views.html.search(adverts, mainPhotos, pagesCount, normalized))
  }

  def advert(advertId: Int) = {
    val advert = advertService.getAdvert(advertId)
    if (advert == null) {
      throw new IllegalArgumentException("Объявление не найдено")
    }

    val photos = photoService.getPhotos(advert).asScala
    Ok(views.html.advert(advert, photos))
  }

}
