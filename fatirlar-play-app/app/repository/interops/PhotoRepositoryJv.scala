package repository.interops

import java.util

import doobie.imports._
import fs2.interop.cats._
import model.Photo
import repository.{DbConnection, PhotoRepository}

import scala.collection.JavaConverters._

class PhotoRepositoryJv(dbConnection: DbConnection, photoRepository: PhotoRepository) {

  def createPhoto(photo: Photo): Unit =
    photoRepository.createPhoto(photo)
      .run
      .transact(dbConnection.xa)
      .unsafePerformIO

  def getPhotos(advertIds: Int): util.List[Photo] =
    photoRepository.getPhotos(advertIds)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .asJava

  def getMainPhotos(advertIds: util.List[Int]): util.List[Photo] =
    photoRepository.getMainPhotos(advertIds.asScala.toList)
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .asJava

  def getAllPhotoHashes: util.List[Photo] =
    photoRepository.getAllPhotoHashes
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .asJava
}
