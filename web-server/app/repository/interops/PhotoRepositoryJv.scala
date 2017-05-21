package repository.interops

import java.util
import javax.inject.Inject

import doobie.imports._
import fs2.interop.cats._
import model.Photo
import repository.{DbConnection, PhotoRepository}

import scala.collection.JavaConverters._

class PhotoRepositoryJv @Inject() (dbConnection: DbConnection, photoRepository: PhotoRepository) {

  def createPhoto(photo: Photo): Photo =
    photoRepository.createPhoto(photo)
      .withUniqueGeneratedKeys[Photo]("id", "advertid", "path", "main", "hash")
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

  def getAllPhotos: util.List[Photo] =
    photoRepository.getAllPhotos
      .list
      .transact(dbConnection.xa)
      .unsafePerformIO
      .asJava
}
