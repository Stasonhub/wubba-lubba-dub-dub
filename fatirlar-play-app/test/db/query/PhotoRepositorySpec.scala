package db.query

import db.TestConnection
import doobie.specs2.imports._
import model.Photo
import org.specs2.mutable._
import repository.PhotoRepository

class PhotoRepositorySpec extends Specification with AnalysisSpec {

  def transactor = TestConnection.dbConnection.xa

  val photoRepository = new PhotoRepository

  check(photoRepository.createPhoto(Photo(0, 0, "path", false, 0)))
  check(photoRepository.getPhotos(0))
  check(photoRepository.getMainPhotos(List()))
  check(photoRepository.getAllPhotoHashes)

}