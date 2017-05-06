package db.methods

import db.TestConnection
import db.methods.TestData.{defaultAdvert, defaultPhoto}
import org.specs2.Specification
import org.specs2.specification.BeforeAll
import repository.interops.{AdvertRepositoryJv, PhotoRepositoryJv}
import repository.{AdvertRepository, PhotoRepository}

import scala.collection.JavaConverters._

class PhotoRepositoryJvSpec extends Specification with BeforeAll {

  val advertRepositoryJv = new AdvertRepositoryJv(TestConnection.dbConnection, new AdvertRepository())
  val photoRepositoryJv = new PhotoRepositoryJv(TestConnection.dbConnection, new PhotoRepository())

  def is =
    s2"""
      The photo repository should:
         create photo $createPhoto
         get photos for advertDetail $getPhotosForAdvert
         get main photos for adverts $getMainPhotos
         get all photos hashes $getAllPhotosHashes
      """

  def createPhoto = {
    val advert = advertRepositoryJv.createAdvert(defaultAdvert)
    photoRepositoryJv.createPhoto(defaultPhoto(advert.id)) must not beNull
  }

  def getPhotosForAdvert = {
    val advert = advertRepositoryJv.createAdvert(defaultAdvert)
    val photo1 = photoRepositoryJv.createPhoto(defaultPhoto(advert.id))
    val photo2 = photoRepositoryJv.createPhoto(defaultPhoto(advert.id))
    val photo3 = photoRepositoryJv.createPhoto(defaultPhoto(advert.id))

    val advertPhotos = photoRepositoryJv.getPhotos(advert.id).asScala
    advertPhotos must containAllOf(List(photo1, photo2, photo3))
  }

  def getMainPhotos = {
    val advert1 = advertRepositoryJv.createAdvert(defaultAdvert)

    val photo11 = photoRepositoryJv.createPhoto(defaultPhoto(advert1.id).copy(main = true))
    val photo12 = photoRepositoryJv.createPhoto(defaultPhoto(advert1.id))
    val photo13 = photoRepositoryJv.createPhoto(defaultPhoto(advert1.id))

    val advert2 = advertRepositoryJv.createAdvert(defaultAdvert)

    val photo21 = photoRepositoryJv.createPhoto(defaultPhoto(advert2.id))
    val photo22 = photoRepositoryJv.createPhoto(defaultPhoto(advert2.id))
    val photo23 = photoRepositoryJv.createPhoto(defaultPhoto(advert2.id).copy(main = true))

    val advertMainPhotos = photoRepositoryJv.getMainPhotos(List(advert1.id, advert2.id).asJava).asScala
    advertMainPhotos must containAllOf(List(photo11, photo23))
  }

  def getAllPhotosHashes = {
    val advert1 = advertRepositoryJv.createAdvert(defaultAdvert)

    val photo11 = photoRepositoryJv.createPhoto(defaultPhoto(advert1.id).copy(main = true))
    val photo12 = photoRepositoryJv.createPhoto(defaultPhoto(advert1.id))
    val photo13 = photoRepositoryJv.createPhoto(defaultPhoto(advert1.id))

    val advert2 = advertRepositoryJv.createAdvert(defaultAdvert)

    val photo21 = photoRepositoryJv.createPhoto(defaultPhoto(advert2.id))
    val photo22 = photoRepositoryJv.createPhoto(defaultPhoto(advert2.id))
    val photo23 = photoRepositoryJv.createPhoto(defaultPhoto(advert2.id).copy(main = true))

    val allPhotos = photoRepositoryJv.getAllPhotos.asScala
    allPhotos must containAllOf(List(photo11, photo12, photo13, photo21, photo22, photo23))
  }

  def beforeAll = TestConnection.cleanUpDb

}
