package service

import javax.inject.Inject

import doobie.imports._
import fs2.interop.cats._
import model.Photo
import repository.{DbConnection, PhotoRepository}

class PhotoService @Inject()(dbConnection: DbConnection, photoRepository: PhotoRepository) {

  val matchedPhotos = 2

  val imageHash = new ImagePHash()

  def getPhotos(advertId: Int) =
    photoRepository.getPhotos(advertId)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO

  def getMainPhotos(advertIds: List[Int]) =
    photoRepository.getMainPhotos(advertIds)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .map(photo => photo.advertId -> photo)
      .toMap

  def matchingAdverts(photos: List[Photo]): List[Int] = {
    val photoHashes = photoRepository.getAllPhotos
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .groupBy(_.hash)

    photos
      .map(_.hash)
      .flatMap(hash => photoHashes.filter(photoHash => imageHash.isTheSame(photoHash._1, hash)).values.flatten)
      .groupBy(_.advertId)
      .mapValues(_.size)
      .filter { case (advert, count) => count >= matchedPhotos }
      .keys
      .toList
  }

}
